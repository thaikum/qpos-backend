package org.example.qposbackend.Accounting.Transactions.TranHeader;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface TranHeaderRepository extends JpaRepository<TranHeader, Long> {
    @Query(nativeQuery = true, value = "select * from tran_header where status =:status and Date(posted_date) between :from and :to")
    List<TranHeader> findAllByStatusAndPostedDateBetween(String status, Date from, Date to);

    @Query(nativeQuery = true, value = "update tran_header set status = :status where id in :ids")
    List<TranHeader> updateStatusByIds(String status, List<Long> ids);

    @Query(nativeQuery = true, value = "update tran_header set status = 'VERIFIED', verified_by_id= :userId, verified_date = current_date() where tran_id in :ids")
    void verifyStatusByIds(Long userId, List<Long> ids);
}
