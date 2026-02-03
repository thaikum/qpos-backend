package org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SecondaryTransactionRequest extends SecondaryTransactionBase{
    private Long shopAccountId;
}
