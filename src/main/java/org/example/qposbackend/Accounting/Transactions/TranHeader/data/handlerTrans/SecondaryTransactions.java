package org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans;

import lombok.Builder;
import lombok.Data;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;

@Data
@Builder
public class SecondaryTransactions {
    private ShopAccount account;
    private Double amount;
    private String description;
}
