package org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans;

import lombok.Builder;
import lombok.Data;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class HandlerTran {
    private LocalDate postedDate;
    private Double totalAmount;
    private String description;
    private ShopAccount primaryAccount;
    private TransactionCategory category;
    private List<SecondaryTransactions> secondaryTransactions;
}
