package org.example.qposbackend.hirePurchase.installments;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.SimpleJournal;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.SimpleJournalLine;
import org.example.qposbackend.Accounting.shopAccount.DefaultAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountService;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.hirePurchase.HirePurchase;
import org.example.qposbackend.hirePurchase.HirePurchaseRepository;
import org.example.qposbackend.hirePurchase.HirePurchaseStatus;
import org.example.qposbackend.hirePurchase.installments.data.InstallmentReceiptData;
import org.example.qposbackend.hirePurchase.installments.data.InstallmentRequest;
import org.example.qposbackend.order.HirePurchaseOrderService;
import org.example.qposbackend.order.OrderService;
import org.example.qposbackend.order.SaleOrder;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentService {
  private final InstallmentRepository installmentRepository;
  private final HirePurchaseRepository hirePurchaseRepository;
  private final TranHeaderService tranHeaderService;
  private final AuthUserShopProvider authProvider;
  private final ShopAccountService shopAccountService;
  private final OrderService orderService;
  private final HirePurchaseOrderService hirePurchaseOrderService;

  @Transactional
  public InstallmentReceiptData createInstallationAndBuildReceipt(InstallmentRequest request) {
    Installment installment = createInstallment(request);
    return buildReceipt(installment);
  }

  public InstallmentReceiptData buildReceipt(Installment installment) {
    return InstallmentReceiptData.builder()
        .itemNames(
            installment.getHirePurchaseRef().getOrderItems().stream()
                .map(oi -> oi.getInventoryItem().getItem().getName())
                .toList())
        .receiptNumber(
            String.format(
                "INST-%04d-%04d", installment.getId(), installment.getHirePurchaseRef().getId()))
        .shopName(installment.getCreatedBy().getShop().getName())
        .cashierName(installment.getCreatedBy().getUser().getFirstName())
        .amountRemaining(installment.getHirePurchaseRef().getRemainingAmount())
        .totalExpectedAmount(installment.getHirePurchaseRef().getExpectedTotalPay())
        .totalPaidAmount(installment.getHirePurchaseRef().getTotalPaidAmount())
        .installment(installment)
        .build();
  }

  @Transactional
  public Installment createInstallment(@Valid InstallmentRequest request) {
    HirePurchase hirePurchase =
        hirePurchaseRepository
            .findById(request.getHirePurchaseId())
            .orElseThrow(() -> new RuntimeException("HirePurchase not found"));

    Installment installment =
        Installment.builder()
            .hirePurchaseRef(hirePurchase)
            .amountPaid(request.getAmount())
            .modeOfPayment(request.getModeOfPayment())
            .build();

    TranHeader associatedTransaction =
        processTransactions(installment, hirePurchase.isItemReleased());
    installment.setAssociatedTransaction(associatedTransaction);
    installment = installmentRepository.save(installment);

    Double totalPaidAmount = hirePurchase.getTotalPaidAmount() + request.getAmount();
    HirePurchaseStatus status = hirePurchase.getStatus();

    if (totalPaidAmount.equals(hirePurchase.getExpectedTotalPay())) {
      hirePurchaseOrderService.processHirePurchaseSale(hirePurchase);
      status = HirePurchaseStatus.COMPLETED;
    }

    hirePurchaseRepository.updateStatusAndTotalPaidAmountById(
        status, totalPaidAmount, hirePurchase.getId());
    hirePurchaseRepository.save(hirePurchase);

    return installment;
  }

  public TranHeader processTransactions(Installment installment, boolean itemReleased) {
    if (itemReleased) {
      shopAccountService.getDefaultAccount(DefaultAccount.ACCOUNTS_RECEIVABLE);
    } else {
      shopAccountService.getDefaultAccount(DefaultAccount.CUSTOMER_ADVANCES);
    }

    SimpleJournalLine l1 =
        SimpleJournalLine.builder()
            .accountName(itemReleased ? "ACCOUNTS RECEIVABLE" : "CUSTOMER ADVANCES")
            .amount(installment.getAmountPaid())
            .tranType('C')
            .build();

    SimpleJournalLine l2 =
        SimpleJournalLine.builder()
            .accountName(
                installment.getModeOfPayment() == ModeOfPayment.CASH ? "CASH" : "MOBILE MONEY")
            .amount(installment.getAmountPaid())
            .tranType('D')
            .build();

    SimpleJournal simpleJournal =
        SimpleJournal.builder()
            .particulars(
                String.format(
                    "Lipa mdogo mdogo installment for %s by %s",
                    installment.getHirePurchaseRef().getId(),
                    installment.getHirePurchaseRef().getCustomer().getFullName()))
            .journalLines(new ArrayList<>(List.of(l1, l2)))
            .build();

    return tranHeaderService.createSimpleTransaction(simpleJournal);
  }
}
