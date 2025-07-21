package org.example.qposbackend.order;

import graphql.util.Pair;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountRepository;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.ReturnItemRequest;
import org.example.qposbackend.EOD.EOD;
import org.example.qposbackend.EOD.EODRepository;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceRepository;
import org.example.qposbackend.OffersAndPromotions.Offers.OfferService;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.order.orderItem.OrderItemRepository;
import org.example.qposbackend.order.orderItem.ReturnInward.ReturnInward;
import org.example.qposbackend.order.orderItem.ReturnInward.ReturnInwardRepository;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
  private final OrderRepository orderRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final AccountRepository accountRepository;
  private final TranHeaderService tranHeaderService;
  private final SpringSecurityAuditorAware auditorAware;
  private final OrderItemRepository orderItemRepository;
  private final ReturnInwardRepository returnInwardRepository;
  private final EODRepository eodRepository;
  private final OfferService offerService;
  private final PriceRepository priceRepository;
  private final ShopAccountRepository shopAccountRepository;

  public List<SaleOrder> fetchByDateRange(DateRange dateRange) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

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
  public void processOrder(SaleOrder saleOrder) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    Date saleDate = getSaleDate(userShop.getShop());

    saleOrder.setShop(userShop.getShop());
    saleOrder.setOrderItems(
        saleOrder.getOrderItems().stream()
            .peek(
                (orderItem -> {
                  InventoryItem inventoryItem =
                      inventoryItemRepository
                          .findById(orderItem.getInventoryItem().getId())
                          .orElseThrow(() -> new RuntimeException("No inventory found."));

                  if (orderItem.getDiscount()
                      > inventoryItem.getPriceDetails().getDiscountAllowed()) {
                    throw new NotAcceptableException(
                        "Maximum discount for "
                            + inventoryItem.getItem().getName()
                            + " is "
                            + inventoryItem.getPriceDetails().getDiscountAllowed());
                  }
                  orderItem.setInventoryItem(inventoryItem);
                }))
            .collect(Collectors.toCollection(ArrayList::new)));

    saleOrder = offerService.getOffersToApply(saleOrder).saleOrder(); // get the offers
    List<OrderItem> addedOrderItems = new ArrayList<>();

    for (OrderItem orderItem : saleOrder.getOrderItems()) {
      InventoryItem inventoryItem = orderItem.getInventoryItem();
      orderItem.setPrice(inventoryItem.getPriceDetails().getSellingPrice());

      List<Price> validPrices =
          inventoryItem.getPriceDetails().getPrices().stream()
              .filter(v -> v.getQuantityUnderThisPrice() > 0)
              .toList();

      List<Pair<Integer, Price>> quantityDeduction =
          processAmountDeduction(orderItem.getQuantity(), validPrices);

      List<Price> changedPrices = new ArrayList<>();
      for (int x = 0; x < quantityDeduction.size(); x++) {
        Pair<Integer, Price> pair = quantityDeduction.get(x);
        pair.second.setQuantityUnderThisPrice(pair.second.getQuantityUnderThisPrice() - pair.first);
        changedPrices.add(pair.second);
        if (x == 0) {
          orderItem.setQuantity(pair.first);
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

    addedOrderItems.addAll(saleOrder.getOrderItems());
    saleOrder.setOrderItems(addedOrderItems);
    saleOrder.setDate(saleDate);
    TranHeader tranHeader = makeSale(saleOrder);
    tranHeaderService.saveAndVerifyTranHeader(tranHeader);

    orderRepository.save(saleOrder);
  }

  public List<Pair<Integer, Price>> processAmountDeduction(Integer quantity, List<Price> prices) {
    List<Pair<Integer, Price>> result = new ArrayList<>();
    List<Price> sortedPrices =
        prices.stream().sorted(Comparator.comparingInt(Price::getId)).toList();
    for (Price price : sortedPrices) {
      int quantityDeducted = Math.min(quantity, price.getQuantityUnderThisPrice());
      quantity -= quantityDeducted;
      result.add(new Pair<>(quantityDeducted, price));
      if (quantity == 0) return result;
    }
    log.info("Current quantity is: {}", quantity);
    throw new RuntimeException("Not enough stock");
  }

  @Transactional
  public void returnItem(ReturnItemRequest returnItemRequest) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    OrderItem orderItem =
        orderItemRepository
            .findById(returnItemRequest.orderItemId())
            .orElseThrow(() -> new NoSuchElementException("No item found."));
    SaleOrder saleOrder =
        orderRepository
            .findByOrderItems(orderItem)
            .orElseThrow(() -> new NoSuchElementException("Sale not found."));
    Date saleDate = getSaleDate(userShop.getShop());

    long dateDiff =
        ChronoUnit.DAYS.between(saleOrder.getDate().toInstant(), new Date().toInstant());
    if (Math.abs(dateDiff) > 30) {
      throw new NotAcceptableException("Items cannot be returned after 30 days.");
    } else if (orderItem.getQuantity() < returnItemRequest.quantity()) {
      throw new NotAcceptableException("Quantity returned must not exceed quantity was sold.");
    }

    if (Objects.isNull(orderItem.getReturnInward())) {

      ReturnInward returnInward =
          ReturnInward.builder()
              .quantityReturned(returnItemRequest.quantity())
              .dateReturned(saleDate)
              .dateSold(saleOrder.getDate())
              .returnReason(returnItemRequest.reason())
              .costIncurred(returnItemRequest.chargesIncurred())
              .build();
      returnInward = returnInwardRepository.save(returnInward);
      orderItem.setReturnInward(returnInward);

      orderItemRepository.save(orderItem);
      TranHeader tranHeader =
          returnItemTransactions(orderItem, returnItemRequest.quantity());
      tranHeaderService.saveAndVerifyTranHeader(tranHeader);
    } else {
      ReturnInward returnInward = orderItem.getReturnInward();
      returnInward.setDateReturned(saleDate);
      orderItemRepository.save(orderItem);
    }
  }

  protected TranHeader returnItemTransactions(OrderItem orderItem, int quantity) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    String accountName = "CASH";
    ShopAccount account =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), accountName)
            .orElseThrow(() -> new NoSuchElementException(accountName + " account not found."));

    ShopAccount costOfGoodsAccount =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "COST OF GOODS")
            .orElseThrow(() -> new NoSuchElementException("COST OF GOODS account not found"));

    ShopAccount salesAccount =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "SALES REVENUE")
            .orElseThrow(() -> new NoSuchElementException("SALES REVENUE account not found"));

    ShopAccount inventoryAccount =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "INVENTORY")
            .orElseThrow(() -> new NoSuchElementException("INVENTORY account not found"));

    TranHeader tranHeader =
        TranHeader.builder()
            .status(TransactionStatus.POSTED.name())
            .postedDate(getSaleDate(userShop.getShop()))
            .postedBy(userShop)
            .verifiedBy(userShop.getUser())
            .status(TransactionStatus.UNVERIFIED.name())
            .build();
    List<PartTran> partTranList = new ArrayList<>();
    int partTranNumber = 1;

    for (int x = 0; x < quantity; x++) {
      PartTran tran =
          PartTran.builder()
              .tranType('D')
              .amount(orderItem.getInventoryItem().getPriceDetails().getBuyingPrice())
              .tranParticulars(
                  "(sales) Returned  " + orderItem.getInventoryItem().getItem().getName())
              .shopAccount(inventoryAccount)
              .partTranNumber(partTranNumber++)
              .build();
      partTranList.add(tran);

      tran =
          PartTran.builder()
              .tranType('C')
              .amount(orderItem.getInventoryItem().getPriceDetails().getBuyingPrice())
              .tranParticulars(
                  "(sales) Cost of returned " + orderItem.getInventoryItem().getItem().getName())
              .shopAccount(costOfGoodsAccount)
              .partTranNumber(partTranNumber++)
              .build();
      partTranList.add(tran);

      // Sales
      tran =
          PartTran.builder()
              .tranType('D')
              .amount(orderItem.getPrice() - orderItem.getDiscount())
              .tranParticulars(
                  "(sales) Return of " + orderItem.getInventoryItem().getItem().getName())
              .shopAccount(salesAccount)
              .partTranNumber(partTranNumber++)
              .build();
      partTranList.add(tran);

      tran =
          PartTran.builder()
              .tranType('C')
              .amount(orderItem.getPrice() - orderItem.getDiscount())
              .tranParticulars(
                  "(sales) Return of " + orderItem.getInventoryItem().getItem().getName())
              .shopAccount(account)
              .partTranNumber(partTranNumber++)
              .build();
      partTranList.add(tran);
    }

    tranHeader.setPartTrans(partTranList);
    return tranHeader;
  }

  protected TranHeader makeSale(SaleOrder saleOrder) {

    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    TranHeader tranHeader =
        TranHeader.builder()
            .status(TransactionStatus.POSTED.name())
            .postedDate(getSaleDate(userShop.getShop()))
            .postedBy(userShop)
            .verifiedBy(userShop.getUser())
            .status(TransactionStatus.UNVERIFIED.name())
            .build();

    ShopAccount cashAccount =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "CASH")
            .orElseThrow(() -> new NoSuchElementException("CASH account not found"));

    ShopAccount costOfGoodsAccount =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "COST OF GOODS")
            .orElseThrow(() -> new NoSuchElementException("COST OF GOODS account not found"));

    ShopAccount salesAccount =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "SALES REVENUE")
            .orElseThrow(() -> new NoSuchElementException("SALES REVENUE account not found"));

    ShopAccount inventoryAccount =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "INVENTORY")
            .orElseThrow(() -> new NoSuchElementException("INVENTORY account not found"));

    ShopAccount mobileMoneyAccount =
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "MOBILE MONEY")
            .orElseThrow(() -> new NoSuchElementException("MOBILE MONEY account not found"));

    double amountInCash =
        Objects.isNull(saleOrder.getAmountInCash()) ? 0.0 : (saleOrder.getAmountInCash());
    double amountInMobile =
        Objects.isNull(saleOrder.getAmountInMpesa()) ? 0.0 : (saleOrder.getAmountInMpesa());

    if (amountInCash != 0) {
      amountInCash =
          saleOrder.getOrderItems().stream()
                  .mapToDouble(item -> (item.getPrice() - item.getDiscount()) * item.getQuantity())
                  .sum()
              - amountInMobile;
    }

    List<PartTran> partTranList = new ArrayList<>();
    int partTranNumber = 1;
    for (OrderItem orderItem : saleOrder.getOrderItems()) {
      for (int x = 0; x < orderItem.getQuantity(); x++) {
        PartTran tran =
            PartTran.builder()
                .tranType('C')
                .amount(orderItem.getBuyingPrice())
                .tranParticulars(
                    "(sales) Sold  " + orderItem.getInventoryItem().getItem().getName())
                .shopAccount(inventoryAccount)
                .partTranNumber(partTranNumber++)
                .build();
        partTranList.add(tran);

        tran =
            PartTran.builder()
                .tranType('D')
                .amount(orderItem.getBuyingPrice())
                .tranParticulars(
                    "(sales) Cost of " + orderItem.getInventoryItem().getItem().getName())
                .shopAccount(costOfGoodsAccount)
                .partTranNumber(partTranNumber++)
                .build();
        partTranList.add(tran);

        // Sales
        tran =
            PartTran.builder()
                .tranType('C')
                .amount(orderItem.getPrice() - orderItem.getDiscount())
                .tranParticulars(
                    "(sales) Sale of " + orderItem.getInventoryItem().getItem().getName())
                .shopAccount(salesAccount)
                .partTranNumber(partTranNumber++)
                .build();
        partTranList.add(tran);

        ShopAccount type = null;
        if (amountInCash >= orderItem.getPrice() - orderItem.getDiscount()) {
          type = cashAccount;
          amountInCash -= (orderItem.getPrice() - orderItem.getDiscount());
        } else if (amountInMobile >= orderItem.getPrice() - orderItem.getDiscount()) {
          type = mobileMoneyAccount;
          amountInMobile -= (orderItem.getPrice() - orderItem.getDiscount());
        }

        if (type != null) {
          tran =
              PartTran.builder()
                  .tranType('D')
                  .amount(orderItem.getPrice() - orderItem.getDiscount())
                  .tranParticulars(
                      "(sales) Sale of " + orderItem.getInventoryItem().getItem().getName())
                  .shopAccount(type)
                  .partTranNumber(partTranNumber++)
                  .build();
          partTranList.add(tran);

        } else {
          double cash = (orderItem.getPrice() - orderItem.getDiscount()) - amountInMobile;
          tran =
              PartTran.builder()
                  .tranType('D')
                  .amount(cash)
                  .tranParticulars(
                      "(sales) Sale of " + orderItem.getInventoryItem().getItem().getName())
                  .shopAccount(cashAccount)
                  .partTranNumber(partTranNumber++)
                  .build();
          partTranList.add(tran);

          tran =
              PartTran.builder()
                  .tranType('D')
                  .amount(amountInMobile)
                  .tranParticulars(
                      "(sales) Sale of " + orderItem.getInventoryItem().getItem().getName())
                  .shopAccount(mobileMoneyAccount)
                  .partTranNumber(partTranNumber++)
                  .build();
          partTranList.add(tran);

          amountInMobile = 0.0;
          amountInCash -= cash;
        }
      }
    }
    tranHeader.setPartTrans(partTranList);
    return tranHeader;
  }

  //    @Bean
  private void loadAllSalesInAccount(){
    Account salesAccount =
        accountRepository
            .findByAccountName("SALES REVENUE")
            .orElseThrow(() -> new NoSuchElementException("SALES REVENUE account not found"));

    if (salesAccount.getBalance() != 0) {
      return;
    }
    List<SaleOrder> saleOrders = orderRepository.findAll();

    List<TranHeader> tranHeaders = new ArrayList<>();
    for (SaleOrder saleOrder : saleOrders) {
      TranHeader tranHeader = makeSale(saleOrder);
      tranHeaders.add(tranHeader);
    }

    try {
      tranHeaderService.verifyTransactions(tranHeaders);

    } catch (Exception ignored) {

    }

    log.info("All sales accounted for");
  }

  private Date getSaleDate(Shop shop) {
    Optional<EOD> eodOptional = eodRepository.findLastEODAndShop(shop.getId());

    if (eodOptional.isPresent()) {
      EOD eod = eodOptional.get();
      long dateDiff = ChronoUnit.DAYS.between(LocalDate.now(), eod.getDate());
      if (dateDiff == 0) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        return tomorrow.getTime();
      } else if (Math.abs(dateDiff) > 1) {
        throw new NotAcceptableException(
            "You cannot start sales before performing yesterday's End Of Day.");
      } else {
        return new Date();
      }
    } else {
      return new Date();
    }
  }
}
