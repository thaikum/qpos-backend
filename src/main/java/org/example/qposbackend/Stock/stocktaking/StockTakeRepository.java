package org.example.qposbackend.Stock.stocktaking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTakeRepository extends JpaRepository<StockTake, Long> {
}
