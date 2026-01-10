package org.example.qposbackend.Accounting.Transactions.PartTran;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface PartTranRepository extends JpaRepository<PartTran, Long> {
  @Query(
      nativeQuery = true,
      value =
          """
                  WITH VerifiedPartTrans AS (
                      SELECT pt.*
                      FROM part_tran pt
                      JOIN tran_header th ON th.tran_id = pt.tran_header_id
                      JOIN shop_account sa on pt.shop_account_id = sa.id and sa.shop_id = :shopId
                      JOIN account a ON sa.account_id = a.id
                      WHERE th.status = 'VERIFIED'
                      AND DATE(th.posted_date) BETWEEN DATE(:from) AND DATE(:to)
                      AND a.account_name = :accountName
                  )
                  SELECT vpt.*
                  FROM VerifiedPartTrans vpt
                  WHERE NOT EXISTS (
                      SELECT 1
                      FROM eod_parttrans ep
                      WHERE vpt.id = ep.involved_transactions_id
                  );
                  
                  """)
  List<PartTran> findAllVerifiedByVerifiedDateBetweenAndAccountName(Long shopId,
      LocalDate from, LocalDate to, String accountName);

  List<PartTran> findAllByShopAccountIsNullAndAccountNotNull();
}
