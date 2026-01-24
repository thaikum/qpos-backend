package org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HandlerTranRequest extends HandlerTranBase{
    private Long primaryAccountId;
    private List<SecondaryTransactionRequest> secondaryTransactions;
}
