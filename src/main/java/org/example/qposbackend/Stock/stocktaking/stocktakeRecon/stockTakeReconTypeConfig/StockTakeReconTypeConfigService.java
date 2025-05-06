package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTakeReconTypeConfigService {
  private final StockTakeReconTypeConfigRepository stockTakeReconTypeConfigRepository;

  public void createStockTakeConfig(StockTakeReconTypeConfig stockTakeReconTypeConfig) {
    stockTakeReconTypeConfigRepository.save(stockTakeReconTypeConfig);
  }

  public List<StockTakeReconTypeConfig> getStockTakeReconTypeConfigs() {
      return stockTakeReconTypeConfigRepository.findAllWithAccounts();
  }
}
