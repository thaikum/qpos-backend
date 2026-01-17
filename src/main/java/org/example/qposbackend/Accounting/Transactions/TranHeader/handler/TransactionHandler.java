package org.example.qposbackend.Accounting.Transactions.TranHeader.handler;

import static org.example.qposbackend.Accounting.Transactions.TranHeader.TranType.CREDIT;
import static org.example.qposbackend.Accounting.Transactions.TranHeader.TranType.DEBIT;
import static org.example.qposbackend.Utils.PosObjectsUtil.firstNonNullString;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTranService;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderRepository;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTran;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@RequiredArgsConstructor
public abstract class TransactionHandler {
  private final TranHeaderService tranHeaderService;
  private final AuthUserShopProvider authProvider;
  private final PartTranService partTranService;
  private final TranHeaderRepository tranHeaderRepository;

  public abstract TransactionCategory getCategory();

  public abstract char getPrimaryTranType();

  public char getSecondaryTranType() {
    return getPrimaryTranType() == DEBIT ? CREDIT : DEBIT;
  }

  public TranHeader getTranHeader(HandlerTran handlerTran) {
    UserShop userShop = authProvider.getCurrentUserShop();
    TranHeader tranHeader =
        tranHeaderService.createBaseTranHeader(handlerTran.getPostedDate(), userShop);
    tranHeader.setStatus(TransactionStatus.UNVERIFIED);
    tranHeader.setDescription(handlerTran.getDescription());
    tranHeader.setTotalAmount(handlerTran.getTotalAmount());
    List<PartTran> partTrans = new ArrayList<>();
    partTrans.add(getPrimaryPartTran(handlerTran));
    partTrans.addAll(getSecondaryPartTrans(handlerTran));
    tranHeader.setPartTrans(partTrans);
    return tranHeader;
  }

  @Transactional
  public TranHeader createAndPersistTranHeader(HandlerTran handlerTran) {
    TranHeader tranHeader = getTranHeader(handlerTran);
    return tranHeaderRepository.save(tranHeader);
  }

  private PartTran getPrimaryPartTran(HandlerTran handlerTran) {
    PartTran partTran = partTranService.generatePartTran(
        getPrimaryTranType(),
        handlerTran.getTotalAmount(),
        handlerTran.getDescription(),
        handlerTran.getPrimaryAccount(),
        1);
    partTran.setIsPrimary(true);
    return partTran;
  }

  private List<PartTran> getSecondaryPartTrans(HandlerTran handlerTran) {
    return handlerTran.getSecondaryTransactions().stream()
        .map(
            sec ->
                partTranService.generatePartTran(
                    getSecondaryTranType(),
                    sec.getAmount(),
                    firstNonNullString(sec.getDescription(), handlerTran.getDescription()),
                    sec.getAccount(),
                    2))
        .toList();
  }

  @Autowired
  public void register(ITransactionHandler transactionHandler) {
    transactionHandler.register(getCategory(), this);
  }
}
