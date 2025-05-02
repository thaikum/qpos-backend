package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockTakeReconTypeConfigRepository
    extends JpaRepository<StockTakeReconTypeConfig, Long> {
    Optional<StockTakeReconTypeConfig> findByStockTakeReconType(StockTakeReconType stockTakeReconType);
}
