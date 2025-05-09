package org.example.qposbackend.Stock.stocktaking.stocktakeItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTakeItemService {
  private final StockTakeItemRepository stockTakeItemRepository;

  public void updateStockTakeItem(StockTakeItemDto stockTakeItem) {
    var rowsAffected =
        stockTakeItemRepository.updateStockTakeItemQuantityById(
            stockTakeItem.id(), stockTakeItem.quantity());
    log.info("Rows affected: {} updated method is: {}", rowsAffected, stockTakeItem);
  }
}
