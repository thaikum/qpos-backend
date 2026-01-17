package org.example.qposbackend.Accounting.Transactions.TranHeader.handler;

import lombok.Getter;
import lombok.Setter;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTranService;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderRepository;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTran;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.springframework.stereotype.Service;

import static org.example.qposbackend.Accounting.Transactions.TranHeader.TranType.CREDIT;

@Getter
@Setter
@Service
public class OverageTransactionHandler extends TransactionHandler {
  private HandlerTran handlerTran;

  public OverageTransactionHandler(
      TranHeaderService tranHeaderService,
      AuthUserShopProvider authProvider,
      PartTranService partTranService,
      TranHeaderRepository tranHeaderRepository) {
    super(tranHeaderService, authProvider, partTranService, tranHeaderRepository);
  }

  @Override
  public TransactionCategory getCategory() {
    return TransactionCategory.OVERAGE;
  }

  @Override
  public char getPrimaryTranType() {
    return CREDIT;
  }
}
