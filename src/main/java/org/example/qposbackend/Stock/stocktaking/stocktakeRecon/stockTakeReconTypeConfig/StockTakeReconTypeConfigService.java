package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTakeReconTypeConfigService {
  private final StockTakeReconTypeConfigRepository stockTakeReconTypeConfigRepository;

  public void createStockTakeConfig(StockTakeReconTypeConfig stockTakeReconTypeConfig) {
    stockTakeReconTypeConfigRepository.save(stockTakeReconTypeConfig);
  }

  public void updateStockTakeConfig(Long id, StockTakeReconTypeConfig newConfig) {
    StockTakeReconTypeConfig config =
        stockTakeReconTypeConfigRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("stock-take-type-config not found"));

    config.setStockOverageCause(newConfig.getStockOverageCause());
    config.setCreateSale(newConfig.getCreateSale());
    config.setApplyPenalty(newConfig.getApplyPenalty());
    config.setHasFinancialImpact(newConfig.getHasFinancialImpact());
    config.setPenaltyAccount(newConfig.getPenaltyAccount());
    config.setExpenseAccount(newConfig.getExpenseAccount());
    config.setBalancingAccount(newConfig.getBalancingAccount());

    stockTakeReconTypeConfigRepository.save(config);
  }

  public List<StockTakeReconTypeConfig> getStockTakeReconTypeConfigs() {
    return stockTakeReconTypeConfigRepository.findAllWithAccounts();
  }
}
