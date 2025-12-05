package org.example.qposbackend.Stock;

import org.example.qposbackend.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
  List<Stock> findAllByShop(Shop shop);

  List<Stock> findAllByShopAndPurchaseDateBetween(
      Shop shop, java.util.Date startDate, java.util.Date endDate);
}
