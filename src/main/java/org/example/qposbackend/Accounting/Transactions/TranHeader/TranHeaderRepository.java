package org.example.qposbackend.Accounting.Transactions.TranHeader;

import jakarta.transaction.Transactional;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.IStatisticsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TranHeaderRepository extends JpaRepository<TranHeader, Long> {
  @Query(
      nativeQuery = true,
      value =
          "select distinct th.* from tran_header th join part_tran pt on th.tran_id = pt.tran_header_id join shop_account sc on pt.shop_account_id = sc.id and sc.shop_id =:shopId where th.status =:status and Date(posted_date) between DATE(:from) and DATE(:to) ")
  List<TranHeader> findAllByStatusAndPostedDateBetween(
      Long shopId, String status, Date from, Date to);

  @Query(nativeQuery = true, value = """
    SELECT *
    FROM tran_header
    WHERE shop_id = :shopId
      AND posted_date BETWEEN :from AND :to
      AND status = :status
      AND (:category IS NULL OR tran_category = :category)
    ORDER BY posted_date DESC
    """)
  List<TranHeader> findTransactionsCustom(
          @Param("shopId") Long shopId,
          @Param("status") String status,
          @Param("from") LocalDate from,
          @Param("to") LocalDate to,
          @Param("category") String category
  );

  @Query(nativeQuery = true, value = """
          SELECT status, count(*) as totalCount
          FROM tran_header
          WHERE shop_id = :shopId
            AND posted_date BETWEEN :from AND :to
            AND (:category IS NULL OR tran_category = :category)
          GROUP BY status
          """)
  List<IStatisticsReport> getTransactionStatistics(
          @Param("shopId") Long shopId,
          @Param("from") LocalDate from,
          @Param("to") LocalDate to,
          @Param("category") String category
  );

  @Transactional
  @Modifying
  @Query(
      nativeQuery = true,
      value =
          "update tran_header set status = 'VERIFIED', verified_by_id= :userId, verified_date = current_date() where tran_id in :ids")
  void verifyStatusByIds(Long userId, List<Long> ids);

  @Modifying
  @Query(
      nativeQuery = true,
      value =
          "update tran_header set status = 'DECLINED', rejected_by_id= :userShopId, rejected_date = current_date() where tran_id in :ids")
  void rejectTransactionById(Long userShopId, List<Long> ids);
}
