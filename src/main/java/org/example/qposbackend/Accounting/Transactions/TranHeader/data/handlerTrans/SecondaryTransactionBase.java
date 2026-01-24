package org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans;

import lombok.Data;

@Data
public abstract class SecondaryTransactionBase {
    private Double amount;
    private String description;
}
