package org.example.qposbackend.Stock.stocktaking;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.DTOs.StockTakeDTO;
import org.example.qposbackend.DTOs.StockTakeItemDTO;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItem;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockTakeService {
  private final SpringSecurityAuditorAware springSecurityAuditorAware;
  private final StockTakeRepository stockTakeRepository;
  private final InventoryItemRepository inventoryItemRepository;

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
                                stockTakeItem.getExpected() - stockTakeItem.getQuantity())
                            .id(stockTakeItem.getId())
                            .inventoryItem(stockTakeItem.getInventoryItem())
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
    return createStockTake(stockTakeType, ids, user, date);
  }

  public StockTake createStockTake(
      StockTakeType stockTakeType, Set<Long> ids, User user, Date date) {

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
                    .quantity(0)
                    .inventoryItem(item)
                    .build())
        .toList();
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
