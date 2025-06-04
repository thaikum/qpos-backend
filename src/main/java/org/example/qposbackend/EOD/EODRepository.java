package org.example.qposbackend.EOD;

import org.example.qposbackend.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface EODRepository extends JpaRepository<EOD, Long> {
    @Query("select EOD from EOD where shop =:shop order by date desc limit 1")
    Optional<EOD> findLastEODAndShop(Shop shop);

    @Query("select EOD from EOD where shop =:shop and DATE(date) between DATE(:from) and DATE(:to)")
    List<EOD> findAllByShopAndDateBetween(Shop shop, Date from, Date to);
}
