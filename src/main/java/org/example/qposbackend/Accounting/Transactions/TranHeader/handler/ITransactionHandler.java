package org.example.qposbackend.Accounting.Transactions.TranHeader.handler;

import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;

public interface ITransactionHandler {
    void register(TransactionCategory category, TransactionHandler handler);
}
