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
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTranRequest;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountService;
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
  private final ShopAccountService shopAccountService;

  public abstract TransactionCategory getCategory();

  public abstract char getPrimaryTranType();

  public char getSecondaryTranType() {
    return getPrimaryTranType() == DEBIT ? CREDIT : DEBIT;
  }

  public TranHeader getTranHeader(HandlerTranRequest request) {
    UserShop userShop = authProvider.getCurrentUserShop();

    TranHeader tranHeader =
        TranHeader.builder()
            .postedDate(request.getPostedDate())
            .postedBy(userShop)
            .verifiedBy(userShop)
            .status(TransactionStatus.UNVERIFIED)
            .totalAmount(request.getTotalAmount())
            .description(request.getDescription())
            .tranCategory(getCategory())
            .shop(userShop.getShop())
            .build();

    List<PartTran> partTrans = new ArrayList<>();
    partTrans.add(getPrimaryPartTran(request));
    partTrans.addAll(getSecondaryPartTrans(request));
    tranHeader.setPartTrans(partTrans);
    return tranHeader;
  }

  @Transactional
  public TranHeader createAndPersistTranHeader(HandlerTranRequest handlerTran) {
    TranHeader tranHeader = getTranHeader(handlerTran);
    return tranHeaderRepository.save(tranHeader);
  }

  private PartTran getPrimaryPartTran(HandlerTranRequest handlerTran) {
    ShopAccount ac = shopAccountService.getShopAccountById(handlerTran.getPrimaryAccountId());
    PartTran partTran =
        partTranService.generatePartTran(
            getPrimaryTranType(),
            handlerTran.getTotalAmount(),
            handlerTran.getDescription(),
            ac,
            1);
    partTran.setIsPrimary(true);
    return partTran;
  }

  private List<PartTran> getSecondaryPartTrans(HandlerTranRequest handlerTran) {
    return handlerTran.getSecondaryTransactions().stream()
        .map(
            sec -> {
              ShopAccount ac = shopAccountService.getShopAccountById(sec.getAccountId());
              return partTranService.generatePartTran(
                  getSecondaryTranType(),
                  sec.getAmount(),
                  firstNonNullString(sec.getDescription(), handlerTran.getDescription()),
                  ac,
                  2);
            })
        .toList();
  }

  @Autowired
  public void register(ITransactionHandler transactionHandler) {
    transactionHandler.register(getCategory(), this);
  }
}
