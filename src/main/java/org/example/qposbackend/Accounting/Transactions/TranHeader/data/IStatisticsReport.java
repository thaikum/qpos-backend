package org.example.qposbackend.Accounting.Transactions.TranHeader.data;

import org.example.qposbackend.Accounting.Transactions.TransactionStatus;

public interface IStatisticsReport {
    Integer getTotalCount();
    TransactionStatus getStatus();
}
