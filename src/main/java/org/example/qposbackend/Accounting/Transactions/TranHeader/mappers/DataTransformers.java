package org.example.qposbackend.Accounting.Transactions.TranHeader.mappers;

import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.SecondaryTransactions;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;

import java.util.List;

import static org.example.qposbackend.Utils.PosObjectsUtil.firstNonNull;

public class DataTransformers {
  public static HandlerTran tranHeaderToHandlerTran(TranHeader tranHeader) {
    HandlerTran handlerTran =
        HandlerTran.builder()
            .postedDate(tranHeader.getPostedDate())
            .totalAmount(tranHeader.getTotalAmount())
            .description(tranHeader.getDescription())
            .category(
                firstNonNull(tranHeader.getTranCategory(), TransactionCategory.MANUAL_JOURNAL))
            .build();

    List<SecondaryTransactions> trans =
        tranHeader.getPartTrans().stream()
            .filter(pt -> Boolean.FALSE.equals(firstNonNull(pt.getIsPrimary(), false)))
            .map(
                pt ->
                    SecondaryTransactions.builder()
                        .account(pt.getShopAccount())
                        .amount(pt.getAmount())
                        .description(pt.getTranParticulars())
                        .build())
            .collect(java.util.stream.Collectors.toList());

    if (trans.size() == tranHeader.getPartTrans().size()) {
      handlerTran.setPrimaryAccount(tranHeader.getPartTrans().getFirst().getShopAccount());
      trans.removeFirst();
      handlerTran.setSecondaryTransactions(trans);
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
