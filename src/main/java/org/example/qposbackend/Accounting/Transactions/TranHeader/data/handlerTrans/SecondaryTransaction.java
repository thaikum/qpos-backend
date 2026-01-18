package org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;

@Data
@EqualsAndHashCode(callSuper = true)
public class SecondaryTransaction extends SecondaryTransactionBase{
    private ShopAccount account;
}
