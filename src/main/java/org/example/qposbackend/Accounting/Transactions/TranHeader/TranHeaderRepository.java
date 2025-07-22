package org.example.qposbackend.Accounting.Transactions.TranHeader;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface TranHeaderRepository extends JpaRepository<TranHeader, Long> {
  @Query(
      nativeQuery = true,
      value =
          "select th.* from tran_header th join part_tran pt on th.tran_id = pt.tran_header_id join shop_account sc on pt.shop_account_id = sc.id and sc.shop_id =:shopId where status =:status and Date(posted_date) between DATE(:from) and DATE(:to) ")
  List<TranHeader> findAllByStatusAndPostedDateBetween(
      Long shopId, String status, Date from, Date to);

  @Query(
      nativeQuery = true,
      value =
          "update tran_header set status = 'VERIFIED', verified_by_id= :userId, verified_date = current_date() where tran_id in :ids")
  void verifyStatusByIds(Long userId, List<Long> ids);
}
