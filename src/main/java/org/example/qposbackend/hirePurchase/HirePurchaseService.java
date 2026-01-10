package org.example.qposbackend.hirePurchase;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.example.qposbackend.Exceptions.GenericExceptions;
import org.example.qposbackend.hirePurchase.data.HirePurchaseResponse;
import org.example.qposbackend.hirePurchase.mapper.HirePurchaseMapper;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.order.orderItem.data.OrderItemResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.customer.Customer;
import org.example.qposbackend.customer.CustomerRepository;
import org.example.qposbackend.hirePurchase.data.HirePurchaseRequest;
import org.example.qposbackend.hirePurchase.installments.Installment;
import org.example.qposbackend.hirePurchase.installments.InstallmentRepository;
import org.example.qposbackend.hirePurchase.installments.InstallmentService;
import org.example.qposbackend.hirePurchase.installments.data.InstallmentReceiptData;
import org.example.qposbackend.order.HirePurchaseOrderService;
import org.example.qposbackend.order.orderItem.OrderItemService;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HirePurchaseService {
  private final HirePurchaseRepository hirePurchaseRepository;
  private final InstallmentRepository installmentRepository;
  private final CustomerRepository customerRepository;
  private final AuthUserShopProvider authProvider;
  private final InstallmentService installmentService;
  private final OrderItemService orderItemService;
  private final HirePurchaseOrderService hirePurchaseOrderService;
  private final HirePurchaseMapper hirePurchaseMapper;

  @Transactional
  public InstallmentReceiptData createHirePurchaseAndPrintReceipt(HirePurchaseRequest request) {
    HirePurchaseResponse response = createHirePurchase(request);
    HirePurchase hp =
        hirePurchaseRepository
            .findById(response.getId())
            .orElseThrow(
                () ->
                    new NoSuchElementException("Lipa mdogo mdogo order not found after creation"));
    return installmentService.buildReceipt(hp.getInstallments().getFirst());
  }

  @Transactional
  public HirePurchaseResponse createHirePurchase(HirePurchaseRequest request) {
    Shop shop = authProvider.getCurrentShop();
    Customer customer =
        customerRepository
            .findCustomerByIdAndShop_Id(request.getCustomerId(), shop.getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        String.format(
                            "User with id %s does not exist for the current shop!",
                            request.getCustomerId())));
    var existing =
        hirePurchaseRepository.findAllByStatusAndShopAndCustomer(
            HirePurchaseStatus.ACTIVE, shop, customer);

    if (!existing.isEmpty()) {
      throw new GenericExceptions(
          String.format(
              "Customer %s has an outstanding balance. Clear the existing amount first to enjoy this service.",
              customer.getFullName()));
    }

    HirePurchase hirePurchase = createHirePurchaseObject(request, shop, customer);
    hirePurchase = hirePurchaseRepository.save(hirePurchase);

    Installment installment =
        Installment.builder()
            .hirePurchaseRef(hirePurchase)
            .amountPaid(request.getInitialPayment())
            .modeOfPayment(request.getModeOfPayment())
            .build();
    TranHeader associatedTransaction =
        installmentService.processTransactions(installment, hirePurchase.isItemReleased());
    installment.setAssociatedTransaction(associatedTransaction);
    installment = installmentRepository.save(installment);
    hirePurchase.setInstallments(List.of(installment));
    if (hirePurchase.isItemReleased()) {
      hirePurchaseOrderService.processHirePurchaseSale(hirePurchase);
    }
    return hirePurchaseMapper.toResponse(hirePurchase);
  }

  private HirePurchase createHirePurchaseObject(
      HirePurchaseRequest request, Shop shop, Customer customer) {

    HirePurchase hp =
        HirePurchase.builder()
            .shop(shop)
            .customer(customer)
            .expectedCompletionDate(request.getExpectedCompletionDate())
            .status(HirePurchaseStatus.ACTIVE)
            .orderItems(
                orderItemService.createAndSaveOrderItems(
                    request.getOrderItems(), request.isItemReleased()))
            .startDate(LocalDate.now())
            .interestApplied(0D)
            .totalPaidAmount(request.getInitialPayment())
            .itemReleased(request.isItemReleased())
            .build();
    hp.setExpectedTotalPay(
        hp.getOrderItems().stream()
            .mapToDouble(oi -> (oi.getPrice() - oi.getDiscount()) * oi.getQuantity())
            .sum());
    return hp;
  }

  public List<HirePurchaseResponse> getAllByDate(LocalDate from, LocalDate to) {
    return hirePurchaseRepository.findAllByStartDateBetween(from, to).stream()
        .map(hirePurchaseMapper::toResponse)
        .toList();
  }

  public HirePurchaseResponse getById(Long id) {
    return hirePurchaseRepository
        .findById(id)
        .map(hirePurchaseMapper::toResponse)
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    String.format("Lipa mdogo mdogo order by id %d is not found", id)));
  }

  private OrderItemResponse mapToOrderItemResponse(OrderItem oi) {
    return OrderItemResponse.builder()
        .itemName(oi.getInventoryItem().getItem().getName())
        .quantity(oi.getQuantity())
        .price(oi.getPrice())
        .buyingPrice(oi.getInventoryItem().getPriceDetails().getBuyingPrice())
        .discount(oi.getDiscount())
        .build();
  }

  public Double getTotalDebtPerCustomer(Customer customer) {
    List<HirePurchase> allOpenDebts =
        hirePurchaseRepository.findAllByStatusAndShopAndCustomer(
            HirePurchaseStatus.ACTIVE, authProvider.getCurrentShop(), customer);
    return allOpenDebts.stream().mapToDouble(HirePurchase::getRemainingAmount).sum();
  }
}
