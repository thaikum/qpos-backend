package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockTakeReconTypeConfigRepository
    extends JpaRepository<StockTakeReconTypeConfig, Long> {
    Optional<StockTakeReconTypeConfig> findByStockTakeReconType(StockTakeReconType stockTakeReconType);
    @Query("SELECT c FROM StockTakeReconTypeConfig c " +
            "LEFT JOIN FETCH c.balancingAccount " +
            "LEFT JOIN FETCH c.penaltyAccount " +
            "LEFT JOIN FETCH c.expenseAccount")
    List<StockTakeReconTypeConfig> findAllWithAccounts();
}
