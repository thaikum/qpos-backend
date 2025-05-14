package org.example.qposbackend.Stock.stocktaking;

import jakarta.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.DTOs.*;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.Order.OrderItem.OrderItem;
import org.example.qposbackend.Order.OrderService;
import org.example.qposbackend.Order.SaleOrder;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItem;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.StockTakeRecon;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.singleItemRecon.SingleItemRecon;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfig;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class StockTakeService {
  private final SpringSecurityAuditorAware springSecurityAuditorAware;
  private final StockTakeRepository stockTakeRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final StockTakeReconTypeConfigRepository stockTakeReconTypeConfigRepository;
  private final AccountRepository accountRepository;
  private final OrderService orderService;
  private final TranHeaderService tranHeaderService;

  public List<StockTake> getStockTakes() {
    return stockTakeRepository.findAll();
  }

  public StockTake getStockTake(Long id) {
    return stockTakeRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("Stock Take Not Found"));
  }

  public void performStockTake(StockTake stockTake) {
    stockTakeRepository.save(stockTake);
  }

  public StockTakeDTO getDiscrepancies(Long stockTakeId) {
    StockTake stockTake =
        stockTakeRepository
            .findById(stockTakeId)
            .orElseThrow(() -> new NoSuchElementException("StockTake not found"));

    return StockTakeDTO.builder()
        .stockTakeId(stockTake.getId())
        .stockTakeDate(stockTake.getStockTakeDate())
        .stockTaker(stockTake.getAssignedUser())
        .stockTakeItems(
            stockTake.getStockTakeItems().stream()
                .filter(
                    stockTakeItem ->
                        !Objects.equals(stockTakeItem.getQuantity(), stockTakeItem.getExpected()))
                .map(
                    stockTakeItem ->
                        StockTakeItemDTO.builder()
                            .quantityDifference(
                                stockTakeItem.getQuantity() - stockTakeItem.getExpected())
                            .id(stockTakeItem.getId())
                            .itemName(stockTakeItem.getInventoryItem().getItem().getName())
                            .itemPrice(
                                stockTakeItem
                                    .getInventoryItem()
                                    .getPriceDetails()
                                    .getSellingPrice())
                            .actualQuantity(stockTakeItem.getQuantity())
                            .expectedQuantity(stockTakeItem.getExpected())
                            .amountDifference(stockTakeItem.getAmountDifference())
                            .build())
                .toList())
        .build();
  }

  public StockTake createStockTake(StockTakeType stockTakeType, Set<Long> ids, Date date) {
    User user =
        springSecurityAuditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not logged in"));
    return createStockTake(stockTakeType, ids, date, user);
  }

  public StockTake createStockTake(
      StockTakeType stockTakeType, Set<Long> ids, Date date, User user) {

    StockTake stockTake =
        StockTake.builder()
            .stockTakeType(stockTakeType)
            .assignedUser(user)
            .stockTakeDate(date)
            .stockTakeItems(createStockTakeList(stockTakeType, ids))
            .build();

    return stockTakeRepository.save(stockTake);
  }

  private List<StockTakeItem> createStockTakeList(StockTakeType stockTakeType, Set<Long> ids) {
    List<InventoryItem> inventoryItems =
        inventoryItemRepository.findInventoryItemByIsDeleted(false);

    if (stockTakeType == StockTakeType.RANDOM) {
      Long n = ids.stream().findFirst().orElse(0L);
      inventoryItems = pickRandom(inventoryItems, n);
    }

    return inventoryItems.stream()
        .filter(
            inventoryItem ->
                switch (stockTakeType) {
                  case FULL, RANDOM -> true;
                  case ITEMS -> ids.contains(inventoryItem.getId());
                  case CATEGORY ->
                      ids.contains(
                          inventoryItem.getItem().getSubCategoryId().getCategory().getId());
                  case SUB_CATEGORY ->
                      ids.contains(inventoryItem.getItem().getSubCategoryId().getId());
                })
        .map(
            item ->
                StockTakeItem.builder()
                    .expected(item.getQuantity())
                    .quantity(null)
                    .inventoryItem(item)
                    .build())
        .toList();
  }

  @Transactional
  public StockTakeDTO reconcileStockTake(StockTakeReconRequest stockTakeReconRequest) {
    List<InventoryItem> inventoryItems = new ArrayList<>();
    StockTake stockTake =
        stockTakeRepository
            .findById(stockTakeReconRequest.getStockTakeId())
            .orElseThrow(() -> new NoSuchElementException("StockTake not found"));
    List<StockTakeRecon> stockTakeRecons = new ArrayList<>(stockTake.getStockTakeRecons());

    for (var groupItemsStockRecon : stockTakeReconRequest.getGroupItemsStockTakeRecons()) {
      StockTakeReconTypeConfig stockTakeReconTypeConfig =
          stockTakeReconTypeConfigRepository
              .findByStockTakeReconType(groupItemsStockRecon.getStockTakeReconType())
              .orElseThrow(() -> new NoSuchElementException("StockTakeReconType is invalid"));

      if (stockTakeReconTypeConfig.getHasFinancialImpact()) {
        var data =
            processFinancialImpactRecon(groupItemsStockRecon, stockTakeReconTypeConfig, stockTake);
        stockTakeRecons.add(data.stockTakeRecon());
        inventoryItems.addAll(data.inventoryItems());
      } else {
        var data = processNonFinancialImpactRecon(groupItemsStockRecon, stockTake);
        stockTakeRecons.add(data.stockTakeRecon());
        inventoryItems.addAll(data.inventoryItems());
      }
    }

    stockTake.setStockTakeRecons(stockTakeRecons);
    inventoryItemRepository.saveAll(inventoryItems);
    stockTakeRepository.save(stockTake);
    return getDiscrepancies(stockTake.getId());
  }

  private StockTakeReconData createReconDataRecord(GroupItemsStockTakeRecon groupItemsStockRecon) {
    List<InventoryItem> inventoryItems = new ArrayList<>();
    List<SingleItemRecon> singleItemRecons = new ArrayList<>();
    var stockTakeRecon =
        StockTakeRecon.builder()
            .reconDate(new Date())
            .stockTakeReconType(groupItemsStockRecon.getStockTakeReconType())
            .reconciliationAmount(0D)
            .singleItemRecons(singleItemRecons)
            .build();

    return new StockTakeReconData(stockTakeRecon, inventoryItems);
  }

  private StockTakeReconData processFinancialImpactRecon(
      GroupItemsStockTakeRecon groupItemsStockRecon,
      StockTakeReconTypeConfig config,
      StockTake stockTake) {
    var data = createReconDataRecord(groupItemsStockRecon);
    var stockTakeRecon = data.stockTakeRecon();
    var inventoryItems = data.inventoryItems();
    var singleItemRecons = stockTakeRecon.getSingleItemRecons();
    List<PartTranDTO> partTranDTOS = new ArrayList<>();
    TranHeaderDTO tranHeaderDTO = new TranHeaderDTO(new Date(), partTranDTOS);

    if (ObjectUtils.nullSafeEquals(groupItemsStockRecon.getPenalty(), 0d)) {
      var penaltyTranDTO = processPenalty(groupItemsStockRecon, config.getPenaltyAccount());
      partTranDTOS.addAll(penaltyTranDTO.partTrans());
    }

    SaleOrder saleOrder = null;
    if (config.getCreateSale()) {
      saleOrder = new SaleOrder();
      saleOrder.setAmountInCash(0d);
      saleOrder.setOrderItems(new ArrayList<>());
      saleOrder.setDate(new Date());
      saleOrder.setModeOfPayment("CASH");
    }

    for (var singleItem : groupItemsStockRecon.getSingleItemStockTakeRecons()) {
      StockTakeItem stockTakeItem =
          getStockTakeItemFromStockTakeById(singleItem.getStockTakeItemId(), stockTake)
              .orElseThrow(() -> new NoSuchElementException("StockTakeItem not found"));

      if (config.getCreateSale()) {
        var salesTransaction =
            processSales(
                stockTakeItem,
                singleItem,
                saleOrder,
                config.getBalancingAccount(),
                groupItemsStockRecon.getDescription());
        partTranDTOS.addAll(salesTransaction.partTrans());
      } else {
        var goodsTransactions =
            processGoodsAccount(
                stockTakeItem,
                config.getExpenseAccount(),
                singleItem,
                groupItemsStockRecon.getDescription());
        partTranDTOS.addAll(goodsTransactions.partTrans());

        // update inventory
        InventoryItem inventoryItem = stockTakeItem.getInventoryItem();
        inventoryItem.getPriceDetails().adjustInventoryQuantity(singleItem.getQuantity());
        inventoryItems.add(inventoryItem);
      }

      SingleItemRecon singleItemRecon =
          SingleItemRecon.builder()
              .stockTakeItem(stockTakeItem)
              .quantity(singleItem.getQuantity())
              .build();
      singleItemRecons.add(singleItemRecon);

      stockTakeRecon.setReconciliationAmount(
          stockTakeRecon.getReconciliationAmount()
              + singleItem.getQuantity()
                  * stockTakeItem.getInventoryItem().getPriceDetails().getSellingPrice());
    }

    if (saleOrder != null) {
      orderService.processOrder(saleOrder);
    }
    tranHeaderService.createTransactions(tranHeaderDTO);

    return data;
  }

  private TranHeaderDTO processSales(
      StockTakeItem stockTakeItem,
      SingleItemStockTakeRecon singleItem,
      SaleOrder saleOrder,
      Account reconAccount,
      String description) {
    double total = stockTakeItem.getInventoryItem().getSellingPrice() * singleItem.getQuantity();
    OrderItem orderItem = new OrderItem();
    orderItem.setQuantity(singleItem.getQuantity());
    orderItem.setInventoryItem(stockTakeItem.getInventoryItem());
    saleOrder.getOrderItems().add(orderItem);
    saleOrder.setAmountInCash(saleOrder.getAmountInCash() + total);

    Account cashAccount =
        accountRepository
            .findByAccountName("CASH")
            .orElseThrow(() -> new NoSuchElementException("CASH" + " account not found."));

    var credit = new PartTranDTO('C', total, description, cashAccount.getAccountNumber());
    var debit = new PartTranDTO('D', total, description, reconAccount.getAccountNumber());
    return new TranHeaderDTO(new Date(), List.of(credit, debit));
  }

  private TranHeaderDTO processGoodsAccount(
      StockTakeItem stockTakeItem,
      Account expenseAccount,
      SingleItemStockTakeRecon stockTakeRecon,
      String particulars) {
    Account costOfGoodsAccount =
        accountRepository
            .findByAccountName("COST OF GOODS")
            .orElseThrow(() -> new NoSuchElementException("COST OF GOODS account not found"));

    PartTranDTO credit =
        new PartTranDTO(
            'C',
            stockTakeItem
                .getInventoryItem()
                .getPriceDetails()
                .getTotalBuyingPrice(stockTakeRecon.getQuantity()),
            particulars,
            costOfGoodsAccount.getAccountNumber());
    PartTranDTO debit =
        new PartTranDTO(
            'D',
            stockTakeItem
                .getInventoryItem()
                .getPriceDetails()
                .getTotalBuyingPrice(stockTakeRecon.getQuantity()),
            particulars,
            expenseAccount.getAccountNumber());
    return new TranHeaderDTO(new Date(), List.of(credit, debit));
  }

  private TranHeaderDTO processPenalty(
      GroupItemsStockTakeRecon groupItemsStockTakeRecon, Account penaltyAccount) {
    Account targetAccount =
        accountRepository
            .findById(groupItemsStockTakeRecon.getTargetAccountId())
            .orElseThrow(() -> new NoSuchElementException("Target penalty account not found"));

    PartTranDTO credit =
        new PartTranDTO(
            'C',
            groupItemsStockTakeRecon.getPenalty(),
            "Penalty: " + groupItemsStockTakeRecon.getDescription(),
            targetAccount.getAccountNumber());
    PartTranDTO debit =
        new PartTranDTO(
            'D',
            groupItemsStockTakeRecon.getPenalty(),
            "Penalty: " + groupItemsStockTakeRecon.getDescription(),
            penaltyAccount.getAccountNumber());
    return new TranHeaderDTO(new Date(), List.of(credit, debit));
  }

  private StockTakeReconData processNonFinancialImpactRecon(
      GroupItemsStockTakeRecon groupItemsStockRecon, StockTake stockTake) {
    var data = createReconDataRecord(groupItemsStockRecon);
    var stockTakeRecon = data.stockTakeRecon();
    var inventoryItems = data.inventoryItems();
    var singleItemRecons = stockTakeRecon.getSingleItemRecons();

    for (var singleItem : groupItemsStockRecon.getSingleItemStockTakeRecons()) {
      StockTakeItem stockTakeItem =
          getStockTakeItemFromStockTakeById(singleItem.getStockTakeItemId(), stockTake)
              .orElseThrow(() -> new NoSuchElementException("StockTakeItem not found"));
      InventoryItem inventoryItem = stockTakeItem.getInventoryItem();
      inventoryItem.getPriceDetails().adjustInventoryQuantity(singleItem.getQuantity());
      inventoryItems.add(inventoryItem);

      SingleItemRecon singleItemRecon =
          SingleItemRecon.builder()
              .stockTakeItem(stockTakeItem)
              .quantity(singleItem.getQuantity())
              .build();
      singleItemRecons.add(singleItemRecon);

      stockTakeRecon.setReconciliationAmount(
          stockTakeRecon.getReconciliationAmount()
              + singleItem.getQuantity() * inventoryItem.getPriceDetails().getSellingPrice());
    }

    return data;
  }

  private Optional<StockTakeItem> getStockTakeItemFromStockTakeById(
      Long stockTakeId, StockTake stockTake) {
    return stockTake.getStockTakeItems().stream()
        .filter(stockTakeItem -> stockTakeItem.getId().equals(stockTakeId))
        .findFirst();
  }

  public static <T> List<T> pickRandom(List<T> list, Long n) {
    if (n > list.size()) {
      throw new IllegalArgumentException("Not enough elements");
    }
    List<T> result = new ArrayList<>(Math.toIntExact(n));
    List<T> copy = new ArrayList<>(list);
    for (int i = 0; i < n; i++) {
      int randomIndex = ThreadLocalRandom.current().nextInt(copy.size());
      result.add(copy.remove(randomIndex));
    }
    return result;
  }

  private record StockTakeReconData(
      StockTakeRecon stockTakeRecon, List<InventoryItem> inventoryItems) {}
}
