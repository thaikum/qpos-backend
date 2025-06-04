package org.example.qposbackend.Stock.stocktaking;

import jakarta.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.*;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.order.OrderService;
import org.example.qposbackend.order.SaleOrder;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItem;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.StockTakeRecon;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.singleItemRecon.SingleItemRecon;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfig;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
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
    Map<Long, Integer> reconciled = reconciledItemQuantity(stockTake);

    return StockTakeDTO.builder()
        .stockTakeId(stockTake.getId())
        .stockTakeDate(stockTake.getStockTakeDate())
        .stockTaker(stockTake.getAssignedUser())
        .doneRecons(
            stockTake.getStockTakeRecons().stream()
                .map(
                    recon ->
                        GroupItemsStockTakeRecon.builder()
                            .id(recon.getId())
                            .penalty(recon.getPenaltyAmount())
                            .stockTakeReconType(recon.getStockTakeReconType())
                            .singleItemStockTakeRecons(
                                recon.getSingleItemRecons().stream()
                                    .map(
                                        item ->
                                            SingleItemStockTakeRecon.builder()
                                                .stockTakeItemId(item.getStockTakeItem().getId())
                                                .quantity(item.getQuantity())
                                                .build())
                                    .toList())
                            .build())
                .toList())
        .stockTakeItems(
            stockTake.getStockTakeItems().stream()
                .filter(
                    stockTakeItem ->
                        !Objects.equals(stockTakeItem.getQuantity(), stockTakeItem.getExpected()))
                .map(
                    stockTakeItem -> {
                      int reconciledQuantity = reconciled.getOrDefault(stockTakeItem.getId(), 0);
                      int diff = stockTakeItem.getQuantity() - stockTakeItem.getExpected();
                      int quantityDifference =
                          diff > 0 ? diff - reconciledQuantity : diff + reconciledQuantity;

                      return StockTakeItemDTO.builder()
                          .quantityDifference(quantityDifference)
                          .id(stockTakeItem.getId())
                          .itemName(stockTakeItem.getInventoryItem().getItem().getName())
                          .itemPrice(
                              stockTakeItem.getInventoryItem().getPriceDetails().getSellingPrice())
                          .actualQuantity(stockTakeItem.getQuantity())
                          .alreadyReconciled(reconciledQuantity)
                          .expectedQuantity(stockTakeItem.getExpected())
                          .amountDifference(
                              stockTakeItem.getInventoryItem().getPriceDetails().getSellingPrice()
                                  * quantityDifference)
                          .build();
                    })
                .toList())
        .build();
  }

  private Map<Long, Integer> reconciledItemQuantity(StockTake stockTake) {
    Map<Long, Integer> reconciledItemQuantity = new HashMap<>();

    for (var recons : stockTake.getStockTakeRecons()) {
      for (var recon : recons.getSingleItemRecons()) {
        reconciledItemQuantity.compute(
            recon.getStockTakeItem().getId(),
            (key, value) -> {
              if (value == null) {
                return recon.getQuantity();
              } else {
                return value + recon.getQuantity();
              }
            });
      }
    }
    return reconciledItemQuantity;
  }

  public StockTake createStockTake(StockTakeType stockTakeType, Set<Long> ids, Date date) {
    UserShop userShop =
        springSecurityAuditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    return createStockTake(stockTakeType, ids, date, userShop);
  }

  public StockTake createStockTake(
      StockTakeType stockTakeType, Set<Long> ids, Date date, UserShop userShop) {

    StockTake stockTake =
        StockTake.builder()
            .stockTakeType(stockTakeType)
            .assignedUser(userShop.getUser())
            .shop(userShop.getShop())
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
    log.info("Stock take request {}", stockTakeReconRequest);
    StockTake stockTake =
        stockTakeRepository
            .findById(stockTakeReconRequest.getStockTakeId())
            .orElseThrow(() -> new NoSuchElementException("StockTake not found"));
    List<StockTakeRecon> stockTakeRecons = new ArrayList<>(stockTake.getStockTakeRecons());

    for (var groupItemsStockRecon : stockTakeReconRequest.getGroupItemsStockTakeRecons()) {
      log.info("Group recon is: {}", groupItemsStockRecon);
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

    stockTake.getStockTakeRecons().clear();
    stockTake.getStockTakeRecons().addAll(stockTakeRecons);
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
    log.info("Processing financial reconciliation");
    var data = createReconDataRecord(groupItemsStockRecon);
    var stockTakeRecon = data.stockTakeRecon();
    var inventoryItems = data.inventoryItems();
    var singleItemRecons = stockTakeRecon.getSingleItemRecons();
    List<PartTranDTO> partTranDTOS = new ArrayList<>();
    TranHeaderDTO tranHeaderDTO = new TranHeaderDTO(new Date(), partTranDTOS);

    if (!ObjectUtils.nullSafeEquals(groupItemsStockRecon.getPenalty(), 0d)) {
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
      if (singleItem.getQuantity() == 0) {
        continue;
      }

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

        InventoryItem inventoryItem = stockTakeItem.getInventoryItem();
        var quantityChange =
            stockTakeItem.getQuantity() > stockTakeItem.getExpected()
                ? Math.abs(singleItem.getQuantity())
                : -Math.abs(singleItem.getQuantity());
        inventoryItem.getPriceDetails().adjustInventoryQuantity(quantityChange);
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

    tranHeaderService.createAndVerifyTransaction(tranHeaderDTO);

    return data;
  }

  private TranHeaderDTO processSales(
      StockTakeItem stockTakeItem,
      SingleItemStockTakeRecon singleItem,
      SaleOrder saleOrder,
      Account reconAccount,
      String description) {
    double total = stockTakeItem.getInventoryItem().getSellingPrice() * singleItem.getQuantity();
    log.info("Total : {}", total);
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
    log.info("Processing non-financial reconciliation");
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
