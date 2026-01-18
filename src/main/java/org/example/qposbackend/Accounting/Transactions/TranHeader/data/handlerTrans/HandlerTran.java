package org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;

@Data
@EqualsAndHashCode(callSuper = true)
public class HandlerTran extends HandlerTranBase {
  private ShopAccount primaryAccount;
  private Long tranId;
  private TransactionStatus status;
  private List<SecondaryTransaction> secondaryTransactions;
}
