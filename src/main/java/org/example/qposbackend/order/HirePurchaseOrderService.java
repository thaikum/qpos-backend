package org.example.qposbackend.order;

import jakarta.transaction.Transactional;
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
import org.example.qposbackend.EOD.EODDateService;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.hirePurchase.HirePurchase;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.example.qposbackend.constants.Constants.TIME_ZONE;

@Slf4j
@Service
@RequiredArgsConstructor
public class HirePurchaseOrderService {
  private final OrderService orderService;
  private final OrderRepository orderRepository;
  private final ShopAccountService shopAccountService;
  private final AuthUserShopProvider authProvider;
  private final TranHeaderService tranHeaderService;
  private final InventoryItemRepository inventoryItemRepository;
  private final PartTranService partTranService;
  private final EODDateService dateService;

  @Transactional
  public void processHirePurchaseSale(HirePurchase hirePurchase) {
    SaleOrder saleOrder = createSaleOrder(hirePurchase);
    saleOrder = orderRepository.save(saleOrder);
    TranHeader tranHeader =
        generateHirePurchaseTransactions(
            saleOrder, hirePurchase.getId(), hirePurchase.isItemReleased());
    log.info("Hirepurchase tranHeader: {}", tranHeader);
    tranHeaderService.saveAndVerifyTranHeader(tranHeader);
    List<InventoryItem> inventoryItems = new ArrayList<>();

    for (OrderItem orderItem : saleOrder.getOrderItems()) {
      InventoryItem ii = orderItem.getInventoryItem();
      ii.getPriceDetails().adjustInventoryQuantity(-orderItem.getQuantity());
      inventoryItems.add(ii);
    }

    inventoryItemRepository.saveAll(inventoryItems);
  }

  private SaleOrder createSaleOrder(HirePurchase hirePurchase) {
    return SaleOrder.builder()
        .date(LocalDate.now(ZoneId.of(TIME_ZONE)))
        .modeOfPayment("hire-purchase")
        .totalAmount(hirePurchase.getExpectedTotalPay())
        .shop(hirePurchase.getShop())
        .orderItems(new ArrayList<>(hirePurchase.getOrderItems()))
        .build();
  }

  private TranHeader generateHirePurchaseTransactions(
      SaleOrder order, Long hirePurchaseId, boolean goodsInitiallyReleased) {
    Double totalBuyingPrice = order.getTotalBuyingPrice();
    Double totalSellingPrice = order.getTotalSellingPrice();
    UserShop userShop = authProvider.getCurrentUserShop();

    List<PartTran> partTranList =
        new ArrayList<>(
            orderService.createInventoryTransactions(
                totalBuyingPrice,
                String.format(
                    "(hire-purchase) Cost of goods sold #HP-ID#%d, SO-ID#%d",
                    hirePurchaseId, order.getId())));

    ShopAccount sales = shopAccountService.getDefaultAccount(DefaultAccount.SALES_REVENUE);
    ShopAccount receivables =
        shopAccountService.getDefaultAccount(DefaultAccount.ACCOUNTS_RECEIVABLE);
    ShopAccount customerAdvances =
        shopAccountService.getDefaultAccount(DefaultAccount.CUSTOMER_ADVANCES);

    PartTran salesPartTran =
        partTranService.generatePartTran(
            'C',
            totalSellingPrice,
            String.format(
                "(hire-purchase) Sale of goods #HP-ID#%d, SO-ID#%d", hirePurchaseId, order.getId()),
            sales,
            null);

    PartTran deductionPartTran =
        partTranService.generatePartTran(
            'D',
            totalSellingPrice,
            String.format(
                "(hire-purchase) sales settlement #HP-ID#%d, SO-ID#%d",
                hirePurchaseId, order.getId()),
            goodsInitiallyReleased ? receivables : customerAdvances,
            null);
    partTranList.add(salesPartTran);
    partTranList.add(deductionPartTran);

    TranHeader tranHeader =
        tranHeaderService.createBaseTranHeader(
            dateService.getSystemDateOrThrowIfEodNotDone(), userShop);
    tranHeader.setPartTrans(partTranList);
    return tranHeader;
  }
}
