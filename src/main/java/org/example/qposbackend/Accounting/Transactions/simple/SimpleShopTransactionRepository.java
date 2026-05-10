package org.example.qposbackend.Accounting.Transactions.simple;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimpleShopTransactionRepository
    extends JpaRepository<SimpleShopTransaction, Long> {

  @Query(
      """
      select s from SimpleShopTransaction s
      join fetch s.tranHeader
      where s.shop.id = :shopId
      and s.postedDate between :start and :end
      order by s.postedDate desc, s.id desc
      """)
  List<SimpleShopTransaction> findByShopAndPostedDateBetween(
      @Param("shopId") long shopId,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end);
}
