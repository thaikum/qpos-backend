package org.example.qposbackend.Stock.stocktaking;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Accounting.shopAccount.DefaultAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountService;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountRepository;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.*;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.InventoryItem.InventoryItemService;
import org.example.qposbackend.InventoryItem.quantityAdjustment.dto.QuantityAdjustmentDto;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.Stock.stocktaking.data.*;
import org.example.qposbackend.Stock.stocktaking.discrepancy.DiscrepancyCategorization;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItem;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItemService;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfig;
import org.springframework.stereotype.Service;

import static org.example.qposbackend.constants.Constants.TIME_ZONE;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTakeService {
  private final SpringSecurityAuditorAware springSecurityAuditorAware;
  private final StockTakeRepository stockTakeRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final TranHeaderService tranHeaderService;
  private final ShopAccountRepository shopAccountRepository;
  private final ShopAccountService shopAccountService;
  private final SpringSecurityAuditorAware auditorAware;
  private final StockTakeItemService stockTakeItemService;
  private final InventoryItemService inventoryItemService;

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
                    stockTakeItem -> {
                      double reconciledQuantity = 0D;
                      double diff = stockTakeItem.getQuantity() - stockTakeItem.getExpected();
                      double quantityDifference =
                          diff > 0D ? diff - reconciledQuantity : diff + reconciledQuantity;

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

  public StockTake createStockTake(StockTakeType stockTakeType, Set<Long> ids, Date date) {
    UserShop userShop =
        springSecurityAuditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    return createStockTake(stockTakeType, ids, date, userShop);
  }

  public StockTake createStockTake(
      StockTakeType stockTakeType, Set<Long> ids, Date date, UserShop userShop) {
    Set<Long> idSet = ids == null ? Set.of() : ids;

    StockTake stockTake =
        StockTake.builder()
            .stockTakeType(stockTakeType)
            .assignedUser(userShop.getUser())
            .shop(userShop.getShop())
            .stockTakeDate(date)
            .status(StockTakeStatus.SCHEDULED)
            .stockTakeItems(createStockTakeList(stockTakeType, idSet))
            .build();

    return stockTakeRepository.save(stockTake);
  }

  private List<StockTakeItem> createStockTakeList(StockTakeType stockTakeType, Set<Long> ids) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    List<InventoryItem> inventoryItems =
        inventoryItemRepository.findInventoryItemByShop_IdAndIsDeleted(
            userShop.getShop().getId(), false);

    if (stockTakeType == StockTakeType.RANDOM) {
      Long n = ids.stream().findFirst().orElse(0L);
      inventoryItems = pickRandom(inventoryItems, n);
    }

    Map<Long, AbcInventoryClassifier.AbcClass> abc =
        switch (stockTakeType) {
          case ABC_CLASS_A, ABC_CLASS_B, ABC_CLASS_C ->
              AbcInventoryClassifier.classifyByStockValue(inventoryItems);
          default -> Map.of();
        };

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
                  case ABC_CLASS_A -> abc.get(inventoryItem.getId()) == AbcInventoryClassifier.AbcClass.A;
                  case ABC_CLASS_B -> abc.get(inventoryItem.getId()) == AbcInventoryClassifier.AbcClass.B;
                  case ABC_CLASS_C -> abc.get(inventoryItem.getId()) == AbcInventoryClassifier.AbcClass.C;
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
  public void refreshStatusAfterItemCount(Long stockTakeItemId) {
    StockTake stockTake =
        stockTakeRepository
            .findByStockTakeItemId(stockTakeItemId)
            .orElseThrow(() -> new NoSuchElementException("Stock take not found"));
    if (stockTake.getStatus() == StockTakeStatus.SCHEDULED) {
      stockTake.setStatus(StockTakeStatus.IN_PROGRESS);
      stockTakeRepository.save(stockTake);
    }
  }

  @Transactional
  public void completeCounting(long stockTakeId) {
    StockTake stockTake =
        stockTakeRepository
            .findById(stockTakeId)
            .orElseThrow(() -> new NoSuchElementException("Stock take not found"));
    if (stockTake.getStatus() != StockTakeStatus.SCHEDULED
        && stockTake.getStatus() != StockTakeStatus.IN_PROGRESS) {
      throw new IllegalStateException(
          "Counting can only be finished while the stock take is scheduled or in progress.");
    }
    boolean allCounted =
        stockTake.getStockTakeItems().stream().allMatch(i -> i.getQuantity() != null);
    if (!allCounted) {
      throw new IllegalStateException("Enter a counted quantity for every line before finishing.");
    }
    boolean anyDiff =
        stockTake.getStockTakeItems().stream()
            .anyMatch(i -> !Objects.equals(i.getQuantity(), i.getExpected()));
    stockTake.setStatus(anyDiff ? StockTakeStatus.UNRECONCILED : StockTakeStatus.RECONCILED);
    stockTakeRepository.save(stockTake);
  }

  @Transactional
  public void reconcileStockTake(StockTakeRecon stockTakeReconRequest) {
    StockTake stockTake =
        stockTakeRepository
            .findById(stockTakeReconRequest.getStockTakeId())
            .orElseThrow(() -> new NoSuchElementException("Stock take not found"));
    if (stockTake.getStatus() != StockTakeStatus.UNRECONCILED
        && stockTake.getStatus() != StockTakeStatus.PARTIALLY_RECONCILED) {
      throw new IllegalStateException(
          "Reconciliation is only allowed when the stock take is waiting for reconciliation.");
    }

    Map<Long, StockTakeItemReconDto> stockTakeItemReconDtoMap =
        stockTakeReconRequest.getStockTakeItems().stream()
            .collect(
                Collectors.toMap(StockTakeItemReconDto::getStockTakeItemId, Function.identity()));

    List<StockTakeItem> discrepant =
        stockTake.getStockTakeItems().stream()
            .filter(i -> !Objects.equals(i.getQuantity(), i.getExpected()))
            .toList();
    for (StockTakeItem item : discrepant) {
      StockTakeItemReconDto dto = stockTakeItemReconDtoMap.get(item.getId());
      if (dto == null || dto.getDiscrepancyCategoryList() == null) {
        throw new IllegalArgumentException(
            "Provide reconciliation reasons for "
                + item.getInventoryItem().getItem().getName());
      }
      validateDiscrepancyAllocations(item, dto);
    }

    stockTake
        .getStockTakeItems()
        .forEach(
            stockTakeItem -> {
              StockTakeItemReconDto stockTakeItemReconDto =
                  stockTakeItemReconDtoMap.get(stockTakeItem.getId());
              if (stockTakeItemReconDto != null) {
                if (Objects.nonNull(stockTakeItemReconDto.getQuantity())
                    && !stockTakeItemReconDto.getQuantity().equals(stockTakeItem.getQuantity())) {
                  stockTakeItem.setQuantity(stockTakeItemReconDto.getQuantity());
                }
                if (stockTakeItemReconDto.getDiscrepancyCategoryList() != null
                    && !stockTakeItemReconDto.getDiscrepancyCategoryList().isEmpty()) {
                  List<DiscrepancyCategorization> discrepancies =
                      stockTakeItemService.getDiscrepancyCategoryList(stockTakeItemReconDto);
                  stockTakeItem.setDiscrepancyCategorization(discrepancies);
                }
              }
            });

    stockTake = stockTakeRepository.save(stockTake);

    for (StockTakeItem item : stockTake.getStockTakeItems()) {
      syncInventoryToPhysicalCount(stockTake.getId(), item);
    }

    for (StockTakeItem item : stockTake.getStockTakeItems()) {
      if (item.getDiscrepancyCategorization() == null) {
        continue;
      }
      for (DiscrepancyCategorization cat : item.getDiscrepancyCategorization()) {
        postCategorizationAccounting(item, cat);
      }
    }

    stockTake.setStatus(StockTakeStatus.RECONCILED);
    stockTakeRepository.save(stockTake);
  }

  private void validateDiscrepancyAllocations(
      StockTakeItem item, StockTakeItemReconDto dto) {
    double diff = item.getQuantity() - item.getExpected();
    double expectedSum = Math.abs(diff);
    double sum =
        dto.getDiscrepancyCategoryList().stream()
            .mapToDouble(d -> Math.abs(d.getQuantity()))
            .sum();
    if (Math.abs(sum - expectedSum) > 0.0001) {
      throw new IllegalArgumentException(
          "Allocated quantities must equal the stock difference for "
              + item.getInventoryItem().getItem().getName());
    }
  }

  private void syncInventoryToPhysicalCount(Long stockTakeId, StockTakeItem stockTakeItem) {
    InventoryItem inventoryItem =
        inventoryItemRepository
            .findById(stockTakeItem.getInventoryItem().getId())
            .orElseThrow();
    double targetQty = stockTakeItem.getQuantity();
    double delta = targetQty - inventoryItem.getQuantity();
    if (Math.abs(delta) < 1e-6) {
      return;
    }
    inventoryItemService.updateInventoryItemQuantity(
        new QuantityAdjustmentDto(
            targetQty, "Stock take #" + stockTakeId + " — counted quantity applied"),
        inventoryItem.getId());
  }

  /**
   * Account to debit vs COGS for shrinkage/write-off. Prefer {@code expenseAccount}; many setups
   * only set {@code balancingAccount} for damaged/missing/internal use (legacy data / UI confusion).
   * Unrecorded-sale types use {@code createSale} + balancing only — do not treat balancing as expense.
   */
  private ShopAccount resolveExpenseAccountForWriteOff(StockTakeReconTypeConfig cfg) {
    if (cfg.getExpenseAccount() != null) {
      return cfg.getExpenseAccount();
    }
    if (!Boolean.TRUE.equals(cfg.getCreateSale()) && cfg.getBalancingAccount() != null) {
      return cfg.getBalancingAccount();
    }
    return null;
  }

  private void postCategorizationAccounting(
      StockTakeItem stockTakeItem, DiscrepancyCategorization cat) {
    StockTakeReconTypeConfig cfg = cat.getReconTypeConfig();
    if (cfg == null || cat.getQuantity() == 0) {
      return;
    }
    double qty = Math.abs(cat.getQuantity());
    ShopAccount expenseSide = resolveExpenseAccountForWriteOff(cfg);
    if (Boolean.TRUE.equals(cfg.getHasFinancialImpact()) && expenseSide != null) {
      TranHeaderDTO journal =
          processGoodsAccount(
              stockTakeItem,
              expenseSide,
              SingleItemStockTakeRecon.builder()
                  .stockTakeItemId(stockTakeItem.getId())
                  .quantity(qty)
                  .build(),
              "Stock take — "
                  + cfg.getStockTakeReconType().name().replace('_', ' ').toLowerCase());
      TranHeader posted = tranHeaderService.createTransactions(journal);
      tranHeaderService.verifyTransaction(posted);
    } else if (Boolean.TRUE.equals(cfg.getHasFinancialImpact()) && expenseSide == null) {
      log.warn(
          "Stock take recon type {} has financial impact but no expense or balancing account; skipping COGS journal.",
          cfg.getStockTakeReconType());
    }
    if (Boolean.TRUE.equals(cfg.getCreateSale()) && cfg.getBalancingAccount() != null) {
      postUnrecordedSaleJournal(stockTakeItem, qty, cfg);
    }
  }

  private void postUnrecordedSaleJournal(
      StockTakeItem stockTakeItem, double qty, StockTakeReconTypeConfig cfg) {
    double revenue =
        qty * stockTakeItem.getInventoryItem().getPriceDetails().getSellingPrice();
    ShopAccount sales = shopAccountService.getDefaultAccount(DefaultAccount.SALES_REVENUE);
    ShopAccount balancing = cfg.getBalancingAccount();
    String p =
        "Stock take — unrecorded sale — "
            + stockTakeItem.getInventoryItem().getItem().getName();
    PartTranDTO creditSale = new PartTranDTO('C', revenue, p, sales.getId());
    PartTranDTO debitBalancing = new PartTranDTO('D', revenue, p, balancing.getId());
    TranHeaderDTO journal =
        new TranHeaderDTO(
            LocalDate.now(ZoneId.of(TIME_ZONE)), List.of(debitBalancing, creditSale), p);
    TranHeader posted = tranHeaderService.createTransactions(journal);
    tranHeaderService.verifyTransaction(posted);
  }

  private TranHeaderDTO processGoodsAccount(
      StockTakeItem stockTakeItem,
      ShopAccount expenseAccount,
      SingleItemStockTakeRecon stockTakeRecon,
      String particulars) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    ShopAccount costOfGoodsAccount =
        shopAccountService.getDefaultAccount(DefaultAccount.COST_OF_GOODS);

    PartTranDTO credit =
        new PartTranDTO(
            'C',
            stockTakeItem
                .getInventoryItem()
                .getPriceDetails()
                .getTotalBuyingPrice(stockTakeRecon.getQuantity()),
            particulars,
            costOfGoodsAccount.getId());

    PartTranDTO debit =
        new PartTranDTO(
            'D',
            stockTakeItem
                .getInventoryItem()
                .getPriceDetails()
                .getTotalBuyingPrice(stockTakeRecon.getQuantity()),
            particulars,
            expenseAccount.getId());
    return new TranHeaderDTO(
        LocalDate.now(ZoneId.of(TIME_ZONE)), List.of(credit, debit), particulars);
  }

  private TranHeaderDTO processPenalty(
      GroupItemsStockTakeRecon groupItemsStockTakeRecon, ShopAccount penaltyAccount) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    ShopAccount targetAccount =
        shopAccountRepository
            .findById(groupItemsStockTakeRecon.getTargetAccountId())
            .orElseThrow(() -> new NoSuchElementException("Target penalty account not found"));

    PartTranDTO credit =
        new PartTranDTO(
            'C',
            groupItemsStockTakeRecon.getPenalty(),
            "Penalty: " + groupItemsStockTakeRecon.getDescription(),
            targetAccount.getId());
    PartTranDTO debit =
        new PartTranDTO(
            'D',
            groupItemsStockTakeRecon.getPenalty(),
            "Penalty: " + groupItemsStockTakeRecon.getDescription(),
            penaltyAccount.getId());
    return new TranHeaderDTO(
        LocalDate.now(ZoneId.of(TIME_ZONE)),
        List.of(credit, debit),
        "Penalty: " + groupItemsStockTakeRecon.getDescription());
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
}
