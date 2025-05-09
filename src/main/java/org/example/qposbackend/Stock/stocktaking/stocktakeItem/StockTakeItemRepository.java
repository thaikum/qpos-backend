package org.example.qposbackend.Stock.stocktaking.stocktakeItem;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTakeItemRepository extends JpaRepository<StockTakeItem, Long> {
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update StockTakeItem sti set sti.quantity = :quantity where sti.id = :id")
  @Transactional
  int updateStockTakeItemQuantityById(Long id, Integer quantity);
}
