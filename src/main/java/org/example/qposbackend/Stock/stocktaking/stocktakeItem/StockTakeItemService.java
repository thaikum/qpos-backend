package org.example.qposbackend.Stock.stocktaking.stocktakeItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Stock.stocktaking.data.StockTakeItemReconDto;
import org.example.qposbackend.Stock.stocktaking.discrepancy.DiscrepancyCategorization;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfig;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTakeItemService {
  private final StockTakeItemRepository stockTakeItemRepository;
  private final StockTakeReconTypeConfigRepository stockTakeReconTypeConfigRepository;

  public void updateStockTakeItem(StockTakeItemDto stockTakeItem) {
    var rowsAffected =
        stockTakeItemRepository.updateStockTakeItemQuantityById(
            stockTakeItem.id(), stockTakeItem.quantity());
    log.info("Rows affected: {} updated method is: {}", rowsAffected, stockTakeItem);
  }

  public void saveStockTakeItem(StockTakeItemReconDto reconDto) {
    StockTakeItem stockTakeItem =
        stockTakeItemRepository.findById(reconDto.getStockTakeItemId()).orElseThrow();
    if (Objects.nonNull(reconDto.getQuantity())
        && !reconDto.getQuantity().equals(stockTakeItem.getQuantity())) {
      stockTakeItem.setQuantity(reconDto.getQuantity());
    }

    List<DiscrepancyCategorization> discrepancies = getDiscrepancyCategoryList(reconDto);
    stockTakeItem.setDiscrepancyCategorization(discrepancies);
    stockTakeItemRepository.save(stockTakeItem);
  }

  public List<DiscrepancyCategorization> getDiscrepancyCategoryList(
      StockTakeItemReconDto reconDto) {
    Map<Long, StockTakeReconTypeConfig> configs =
        stockTakeReconTypeConfigRepository.findAll().stream()
            .collect(Collectors.toMap(StockTakeReconTypeConfig::getId, Function.identity()));
    return reconDto.getDiscrepancyCategoryList().stream()
        .map(
            d ->
                DiscrepancyCategorization.builder()
                    .isReconciled(false)
                    .deductEmployee(d.isDeductEmployee())
                    .reconTypeConfig(configs.get(d.getReconConfigId()))
                    .build())
        .toList();
  }
}
