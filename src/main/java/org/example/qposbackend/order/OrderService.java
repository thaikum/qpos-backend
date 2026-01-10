package org.example.qposbackend.order;

import static org.example.qposbackend.Utils.NumberUtils.zeroIfNull;

import graphql.util.Pair;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTranService;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Accounting.shopAccount.DefaultAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountService;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.EOD.EODDateService;
import org.example.qposbackend.Exceptions.GenericRuntimeException;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceRepository;
import org.example.qposbackend.OffersAndPromotions.Offers.OfferService;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.order.receipt.ReceiptData;
import org.example.qposbackend.order.receipt.ReceiptItem;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
  private static final char DEBIT = 'D';
  private static final char CREDIT = 'C';

  private final OrderRepository orderRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final TranHeaderService tranHeaderService;
  private final SpringSecurityAuditorAware auditorAware;
  private final OfferService offerService;
  private final PriceRepository priceRepository;
  private final ShopAccountService shopAccountService;
  private final PartTranService partTranService;
  private final EODDateService dateService;

  public List<SaleOrder> fetchByDateRange(DateRange dateRange) {
    UserShop userShop = getCurrentUserShop();
    return fetchByShopAndDateRange(userShop.getShop(), dateRange.start(), dateRange.end());
  }

  public List<SaleOrder> fetchByShopAndDateRange(Shop shop, Date start, Date end) {
    List<SaleOrder> ordersWithinRange =
        orderRepository.fetchAllByDateRangeAndShop(start, end, shop.getId());

    List<SaleOrder> returnedItems =
        orderRepository.fetchAllSalesReturnedWithinRangeAndShop(start, end, shop.getId());
    returnedItems.forEach(
        order -> {
          order.setOrderItems(
              order.getOrderItems().stream()
                  .filter(item -> !Objects.isNull(item.getReturnInward()))
                  .toList());
        });

    ordersWithinRange.addAll(returnedItems);
    return ordersWithinRange;
  }

  @Transactional
  public SaleOrder processOrder(SaleOrder saleOrder) {
    UserShop userShop = getCurrentUserShop();
    LocalDate saleDate = dateService.getSystemDateOrThrowIfEodNotDone();

    saleOrder.setShop(userShop.getShop());
    validateAndPrepareOrderItems(saleOrder);

    saleOrder = offerService.getOffersToApply(saleOrder).saleOrder();
    List<OrderItem> addedOrderItems = new ArrayList<>();

    processPrices(saleOrder, addedOrderItems);

    addedOrderItems.addAll(saleOrder.getOrderItems());
    saleOrder.setOrderItems(addedOrderItems);
    saleOrder.setDate(saleDate);

    saleOrder = orderRepository.save(saleOrder);
    TranHeader tranHeader = makeSale(saleOrder);
    log.info("TranHeader: {}", tranHeader);
    tranHeaderService.saveAndVerifyTranHeader(tranHeader);

    return saleOrder;
  }

  private void validateAndPrepareOrderItems(SaleOrder saleOrder) {
    List<OrderItem> preparedItems =
        saleOrder.getOrderItems().stream()
            .map(this::validateAndPrepareOrderItem)
            .collect(Collectors.toCollection(ArrayList::new));
    saleOrder.setOrderItems(preparedItems);
  }

  private OrderItem validateAndPrepareOrderItem(OrderItem orderItem) {
    InventoryItem inventoryItem =
        inventoryItemRepository
            .findById(orderItem.getInventoryItem().getId())
            .orElseThrow(() -> new GenericRuntimeException("No inventory found."));

    double discountAllowed = inventoryItem.getPriceDetails().getDiscountAllowed();
    if (orderItem.getDiscount() > discountAllowed) {
      throw new NotAcceptableException(
          String.format(
              "Maximum discount for %s is %.2f",
              inventoryItem.getItem().getName(), discountAllowed));
    }

    double minimumPerUnit = inventoryItem.getItem().getMinimumPerUnit();
    if (orderItem.getQuantity() < minimumPerUnit) {
      throw new NotAcceptableException(
          String.format(
              "The minimum quantity that can be sold for %s is %.2f",
              inventoryItem.getItem().getName(), minimumPerUnit));
    }

    orderItem.setInventoryItem(inventoryItem);
    return orderItem;
  }

  private UserShop getCurrentUserShop() {
    return auditorAware
        .getCurrentAuditor()
        .orElseThrow(() -> new NoSuchElementException("User not found"));
  }

  private void processPrices(SaleOrder saleOrder, List<OrderItem> addedOrderItems) {
    for (OrderItem orderItem : saleOrder.getOrderItems()) {
      InventoryItem inventoryItem = orderItem.getInventoryItem();
      orderItem.setPrice(inventoryItem.getPriceDetails().getSellingPrice());

      List<Price> validPrices =
          inventoryItem.getPriceDetails().getPrices().stream()
              .filter(v -> v.getQuantityUnderThisPrice() > 0)
              .toList();

      List<Pair<Double, Price>> quantityDeduction =
          inventoryItem
              .getPriceDetails()
              .getBuyingPriceBrokenDownPerTheQuantity(orderItem.getQuantity());

      List<Price> changedPrices = new ArrayList<>();
      for (int x = 0; x < quantityDeduction.size(); x++) {
        Pair<Double, Price> pair = quantityDeduction.get(x);
        pair.second.setQuantityUnderThisPrice(pair.second.getQuantityUnderThisPrice() - pair.first);
        changedPrices.add(pair.second);
        if (x == 0) {
          orderItem.setQuantity(pair.first);
          orderItem.setBuyingPrice(pair.second.getBuyingPrice());
        } else {
          OrderItem orderItem1 =
              OrderItem.builder()
                  .buyingPrice(pair.second.getBuyingPrice())
                  .price(orderItem.getPrice())
                  .discount(orderItem.getDiscount())
                  .quantity(pair.first)
                  .inventoryItem(inventoryItem)
                  .discountMode(orderItem.getDiscountMode())
                  .build();
          addedOrderItems.add(orderItem1);
        }
      }

      inventoryItem = inventoryItemRepository.save(inventoryItem);
      orderItem.setInventoryItem(inventoryItem);
      priceRepository.saveAll(changedPrices);
    }
  }

  protected TranHeader makeSale(SaleOrder saleOrder) {
    UserShop userShop = getCurrentUserShop();
    TranHeader tranHeader =
        tranHeaderService.createBaseTranHeader(
            dateService.getSystemDateOrThrowIfEodNotDone(), userShop);

    double totalAmount = saleOrder.getTotalSellingPrice();
    double amountInCredit = Math.min(zeroIfNull(saleOrder.getAmountInCredit()), totalAmount);
    double amountInMobile =
        Math.min(zeroIfNull(saleOrder.getAmountInMpesa()), totalAmount - amountInCredit);
    double amountInCash =
        Math.min(
            zeroIfNull(saleOrder.getAmountInCash()), totalAmount - amountInMobile - amountInCredit);
    double totalBuyingPrice = saleOrder.getTotalBuyingPrice();

    List<PartTran> partTranList =
        new ArrayList<>(
            createInventoryTransactions(
                totalBuyingPrice, String.format("(sales) Cost of order #%d", saleOrder.getId())));

    partTranList.addAll(
        debitAssetAccounts(
            amountInCash,
            amountInMobile,
            amountInCredit,
            "(sales) Sale of order #%d" + saleOrder.getId()));

    partTranList.add(
        partTranService.generatePartTran(
            CREDIT,
            totalAmount,
            String.format("(sales) Sale of order #%d", saleOrder.getId()),
            shopAccountService.getDefaultAccount(DefaultAccount.SALES_REVENUE),
            0));

    tranHeader.setPartTrans(partTranList);
    return tranHeader;
  }

  private List<PartTran> debitAssetAccounts(
      Double amountInCash, Double amountInMpesa, Double amountInCredit, String particulars) {
    List<PartTran> partTrans = new ArrayList<>();

    addPartTranIfNonZero(partTrans, amountInCash, DefaultAccount.CASH, particulars);
    addPartTranIfNonZero(partTrans, amountInMpesa, DefaultAccount.MOBILE_MONEY, particulars);
    addPartTranIfNonZero(
        partTrans, amountInCredit, DefaultAccount.ACCOUNTS_RECEIVABLE, particulars);

    return partTrans;
  }

  private void addPartTranIfNonZero(
      List<PartTran> partTrans, Double amount, DefaultAccount accountType, String particulars) {
    if (amount != 0D) {
      ShopAccount account = shopAccountService.getDefaultAccount(accountType);
      PartTran tran = partTranService.generatePartTran(DEBIT, amount, particulars, account, null);
      partTrans.add(tran);
    }
  }

  public List<PartTran> createInventoryTransactions(Double totalBuyingPrice, String particulars) {
    ShopAccount costOfGoodsAccount =
        shopAccountService.getDefaultAccount(DefaultAccount.COST_OF_GOODS);

    ShopAccount inventoryAccount = shopAccountService.getDefaultAccount(DefaultAccount.INVENTORY);

    PartTran tran1 =
        partTranService.generatePartTran(
            CREDIT, totalBuyingPrice, particulars, inventoryAccount, 0);
    PartTran tran2 =
        partTranService.generatePartTran(
            DEBIT, totalBuyingPrice, particulars, costOfGoodsAccount, 1);

    return List.of(tran1, tran2);
  }

  public ReceiptData generateReceipt(Long orderId) {
    SaleOrder order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new NoSuchElementException("Order not found"));

    Shop shop = order.getShop();
    return ReceiptData.builder()
        .shopName(shop.getName())
        .shopTagline(shop.getTagLine())
        .shopPhone(shop.getPhone())
        .receiptNo(order.getId().toString())
        .orderNo(order.getId().toString())
        .receiptItems(
            order.getOrderItems().stream()
                .map(
                    oi ->
                        ReceiptItem.builder()
                            .itemName(oi.getInventoryItem().getItem().getName())
                            .quantity(oi.getQuantity())
                            .unitPrice(oi.getPrice() - oi.getDiscount())
                            .total((oi.getPrice() - oi.getDiscount()) * oi.getQuantity())
                            .build())
                .toList())
        .receiptTotal(
            order.getOrderItems().stream()
                .mapToDouble(oi -> (oi.getPrice() - oi.getDiscount()) * oi.getQuantity())
                .sum())
        .build();
  }
}
