package org.example.qposbackend.Stock.stocktaking;

import jakarta.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountRepository;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.*;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.Stock.stocktaking.data.*;
import org.example.qposbackend.Stock.stocktaking.discrepancy.DiscrepancyCategorization;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItem;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItemService;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfig;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfigRepository;
import org.example.qposbackend.order.OrderService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTakeService {
  private final SpringSecurityAuditorAware springSecurityAuditorAware;
  private final StockTakeRepository stockTakeRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final StockTakeReconTypeConfigRepository stockTakeReconTypeConfigRepository;
  private final OrderService orderService;
  private final TranHeaderService tranHeaderService;
  private final ShopAccountRepository shopAccountRepository;
  private final SpringSecurityAuditorAware auditorAware;
  private final StockTakeItemService stockTakeItemService;

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
  public void reconcileStockTake(StockTakeRecon stockTakeReconRequest) {
    StockTake stockTake =
        stockTakeRepository.findById(stockTakeReconRequest.getStockTakeId()).orElseThrow();

    Map<Long, StockTakeItemReconDto> stockTakeItemReconDtoMap =
        stockTakeReconRequest.getStockTakeItems().stream()
            .collect(
                Collectors.toMap(StockTakeItemReconDto::getStockTakeItemId, Function.identity()));
    stockTake
        .getStockTakeItems()
        .forEach(
            stockTakeItem -> {
              var stockTakeItemReconDto = stockTakeItemReconDtoMap.get(stockTakeItem.getId());
              if (stockTakeItemReconDto != null) {
                if (Objects.nonNull(stockTakeItemReconDto.getQuantity())
                    && !stockTakeItemReconDto.getQuantity().equals(stockTakeItem.getQuantity())) {
                  stockTakeItem.setQuantity(stockTakeItemReconDto.getQuantity());
                }

                List<DiscrepancyCategorization> discrepancies = stockTakeItemService.getDiscrepancyCategoryList(stockTakeItemReconDto);
                stockTakeItem.setDiscrepancyCategorization(discrepancies);
              }
            });
    stockTakeRepository.save(stockTake);
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
        shopAccountRepository
            .findByShopAndAccount_AccountName(userShop.getShop(), "COST OF GOODS")
            .orElseThrow(() -> new NoSuchElementException("COST OF GOODS account not found"));

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
    return new TranHeaderDTO(new Date(), List.of(credit, debit));
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
    return new TranHeaderDTO(new Date(), List.of(credit, debit));
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
