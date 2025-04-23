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
                      JOIN account a ON pt.account_id = a.id
                      WHERE th.status = 'VERIFIED'
                      AND DATE(th.verified_date) BETWEEN DATE(:from) AND DATE(:to)
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
  List<PartTran> findAllVerifiedByVerifiedDateBetweenAndAccountName(
      LocalDate from, LocalDate to, String accountName);
}
