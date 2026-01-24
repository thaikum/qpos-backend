package org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans;

import java.time.LocalDate;
import lombok.Data;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;

@Data
public abstract class HandlerTranBase {
    private LocalDate postedDate;
    private Double totalAmount;
    private String description;
    private TransactionCategory category;
}
