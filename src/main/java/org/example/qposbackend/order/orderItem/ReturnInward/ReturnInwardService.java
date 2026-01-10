package org.example.qposbackend.order.orderItem.ReturnInward;

import static org.example.qposbackend.constants.Constants.TIME_ZONE;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTranService;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Accounting.shopAccount.DefaultAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountService;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.ReturnItemRequest;
import org.example.qposbackend.EOD.EODDateService;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.example.qposbackend.order.OrderRepository;
import org.example.qposbackend.order.SaleOrder;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.order.orderItem.OrderItemRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnInwardService {
  private final OrderItemRepository orderItemRepository;
  private final OrderRepository orderRepository;
  private final ReturnInwardRepository returnInwardRepository;
  private final TranHeaderService tranHeaderService;
  private final ShopAccountService shopAccountService;
  private final AuthUserShopProvider authProvider;
  private final EODDateService dateService;

  private static final char DEBIT = 'D';
  private static final char CREDIT = 'C';
  private final PartTranService partTranService;

  @Transactional
  public void returnItem(ReturnItemRequest returnItemRequest) {
    UserShop userShop = getCurrentUserShop();
    OrderItem orderItem =
        orderItemRepository
            .findById(returnItemRequest.orderItemId())
            .orElseThrow(() -> new NoSuchElementException("No item found."));
    SaleOrder saleOrder =
        orderRepository
            .findByOrderItems(orderItem)
            .orElseThrow(() -> new NoSuchElementException("Sale not found."));
    LocalDate saleDate = dateService.getSystemDateOrThrowIfEodNotDone();

    validateReturnRequest(saleOrder, orderItem, returnItemRequest);

    if (Objects.isNull(orderItem.getReturnInward())) {
      ReturnInward returnInward = createReturnInward(returnItemRequest, saleOrder, saleDate);
      returnInward = returnInwardRepository.save(returnInward);
      orderItem.setReturnInward(returnInward);

      orderItemRepository.save(orderItem);
      TranHeader tranHeader = returnItemTransactions(orderItem, returnItemRequest.quantity());
      tranHeaderService.saveAndVerifyTranHeader(tranHeader);
    } else {
      ReturnInward returnInward = orderItem.getReturnInward();
      returnInward.setDateReturned(saleDate);
      orderItemRepository.save(orderItem);
    }
  }

  private void validateReturnRequest(
      SaleOrder saleOrder, OrderItem orderItem, ReturnItemRequest request) {
    long dateDiff =
        ChronoUnit.DAYS.between(saleOrder.getDate(), LocalDate.now(ZoneId.of(TIME_ZONE)));
    if (Math.abs(dateDiff) > 30) {
      throw new NotAcceptableException("Items cannot be returned after 30 days.");
    }
    if (orderItem.getQuantity() < request.quantity()) {
      throw new NotAcceptableException("Quantity returned must not exceed quantity was sold.");
    }
  }

  private ReturnInward createReturnInward(
      ReturnItemRequest request, SaleOrder saleOrder, LocalDate saleDate) {
    return ReturnInward.builder()
        .quantityReturned(request.quantity())
        .dateReturned(saleDate)
        .dateSold(saleOrder.getDate())
        .returnReason(request.reason())
        .costIncurred(request.chargesIncurred())
        .build();
  }

  protected TranHeader returnItemTransactions(OrderItem orderItem, int quantity) {
    UserShop userShop = getCurrentUserShop();

    ShopAccount cashAccount = shopAccountService.getDefaultAccount(DefaultAccount.CASH);
    ShopAccount costOfGoodsAccount =
        shopAccountService.getDefaultAccount(DefaultAccount.COST_OF_GOODS);
    ShopAccount salesAccount = shopAccountService.getDefaultAccount(DefaultAccount.SALES_REVENUE);
    ShopAccount inventoryAccount = shopAccountService.getDefaultAccount(DefaultAccount.INVENTORY);

    TranHeader tranHeader =
        tranHeaderService.createBaseTranHeader(
            dateService.getSystemDateOrThrowIfEodNotDone(), userShop);
    List<PartTran> partTranList = new ArrayList<>();
    int partTranNumber = 1;

    double buyingPrice = orderItem.getInventoryItem().getPriceDetails().getBuyingPrice();
    double netPrice = orderItem.getPrice() - orderItem.getDiscount();
    String itemName = orderItem.getInventoryItem().getItem().getName();

    for (int x = 0; x < quantity; x++) {
      partTranList.add(
          partTranService.generatePartTran(
              DEBIT,
              buyingPrice,
              "(sales) Returned  " + itemName,
              inventoryAccount,
              partTranNumber++));
      partTranList.add(
          partTranService.generatePartTran(
              CREDIT,
              buyingPrice,
              "(sales) Cost of returned " + itemName,
              costOfGoodsAccount,
              partTranNumber++));
      partTranList.add(
          partTranService.generatePartTran(
              DEBIT, netPrice, "(sales) Return of " + itemName, salesAccount, partTranNumber++));
      partTranList.add(
          partTranService.generatePartTran(
              CREDIT,
              netPrice,
              "(sales) Cash refund for " + itemName,
              cashAccount,
              partTranNumber++));
    }

    tranHeader.setPartTrans(partTranList);
    return tranHeader;
  }

  private UserShop getCurrentUserShop() {
    return authProvider.getCurrentUserShop();
  }
}
