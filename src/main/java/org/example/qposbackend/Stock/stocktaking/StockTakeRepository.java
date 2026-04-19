package org.example.qposbackend.Stock.stocktaking;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTakeRepository extends JpaRepository<StockTake, Long> {

  @Query("SELECT st FROM StockTake st JOIN st.stockTakeItems i WHERE i.id = :stockTakeItemId")
  Optional<StockTake> findByStockTakeItemId(@Param("stockTakeItemId") Long stockTakeItemId);
}
