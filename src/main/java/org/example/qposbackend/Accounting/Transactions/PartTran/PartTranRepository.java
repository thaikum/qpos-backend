package org.example.qposbackend.Accounting.Transactions.PartTran;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface PartTranRepository extends JpaRepository<PartTran, Long> {
    @Query(nativeQuery = true, value = "select pt.* " +
            "from part_tran pt " +
            "         join tran_header th on th.tran_id = pt.tran_header_id " +
            "join qpos.account a on pt.account_id = a.id " +
            "where th.status = 'VERIFIED' " +
            "  and Date (th.verified_date) between DATE (:from) " +
            "  and DATE (:to) " +
            "  and a.account_name = :accountName" +
            " and pt.tran_particulars not like '(sales)%'")
    List<PartTran> findAllNonSaleVerifiedByVerifiedDateBetweenAndAccountName(LocalDate from, LocalDate to, String accountName);
}
