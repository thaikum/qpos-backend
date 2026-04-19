package org.example.qposbackend.Accounting.Transactions;

public enum TransactionStatus {
    UNVERIFIED,
    VERIFIED,
    POSTED,
    DECLINED,
    /** Original journal was negated by a reversal entry; excluded from EOD cash aggregation. */
    REVERSED
}
