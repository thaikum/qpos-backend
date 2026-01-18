package org.example.qposbackend.Accounting.Transactions.TranHeader.mappers;

import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.SecondaryTransaction;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;

import java.util.List;

import static org.example.qposbackend.Utils.PosObjectsUtil.firstNonNull;

public class DataTransformers {
  public static HandlerTran tranHeaderToHandlerTran(TranHeader tranHeader) {
    HandlerTran handlerTran = new HandlerTran();

    handlerTran.setPostedDate(tranHeader.getPostedDate());
    handlerTran.setTotalAmount(tranHeader.getTotalAmount());
    handlerTran.setDescription(tranHeader.getDescription());
    handlerTran.setTranId(tranHeader.getTranId());
    handlerTran.setStatus(tranHeader.getStatus());
    handlerTran.setCategory(
        firstNonNull(tranHeader.getTranCategory(), TransactionCategory.MANUAL_JOURNAL));

    List<SecondaryTransaction> trans =
        tranHeader.getPartTrans().stream()
            .filter(pt -> Boolean.FALSE.equals(firstNonNull(pt.getIsPrimary(), false)))
            .map(
                pt -> {
                  SecondaryTransaction tran = new SecondaryTransaction();
                  tran.setAccount(pt.getShopAccount());
                  tran.setAmount(pt.getAmount());
                  tran.setDescription(pt.getTranParticulars());
                  return tran;
                })
            .collect(java.util.stream.Collectors.toList());

    handlerTran.setSecondaryTransactions(trans);

    if (trans.size() == tranHeader.getPartTrans().size()) {
      handlerTran.setPrimaryAccount(tranHeader.getPartTrans().getFirst().getShopAccount());
      trans.removeFirst();
    } else {
      ShopAccount pAccount =
          tranHeader.getPartTrans().stream()
              .filter(pt -> firstNonNull(pt.getIsPrimary(), false))
              .findFirst()
              .orElseThrow()
              .getShopAccount();
      handlerTran.setPrimaryAccount(pAccount);
    }
    if (handlerTran.getTotalAmount() == null || handlerTran.getTotalAmount() == 0) {
      handlerTran.setTotalAmount(
          tranHeader.getPartTrans().stream().mapToDouble(PartTran::getAmount).sum() / 2);
    }

    return handlerTran;
  }
}
