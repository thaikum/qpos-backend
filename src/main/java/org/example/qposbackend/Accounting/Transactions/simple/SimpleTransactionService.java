package org.example.qposbackend.Accounting.Transactions.simple;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTranRequest;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.SecondaryTransactionRequest;
import org.example.qposbackend.Accounting.Transactions.TranHeader.handler.TranHandlerService;
import org.example.qposbackend.Accounting.shopAccount.DefaultAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountService;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.EOD.EODDateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimpleTransactionService {

  private final TranHandlerService tranHandlerService;
  private final ShopAccountService shopAccountService;
  private final AuthUserShopProvider authProvider;
  private final EODDateService eodDateService;
  private final SimpleShopTransactionRepository simpleShopTransactionRepository;

  @Transactional
  public SimpleTransactionPostResponse record(SimpleTransactionPostRequest request) {
    UserShop userShop = authProvider.getCurrentUserShop();
    ShopAccount primaryShopAccount =
        shopAccountService.getDefaultAccount(primaryDefault(request.getKind()));

    ShopAccount secondaryShopAccount =
        switch (request.getPaymentSource()) {
          case CASH -> shopAccountService.getDefaultAccount(DefaultAccount.CASH);
          case MOBILE_MONEY ->
              shopAccountService.getDefaultAccount(DefaultAccount.MOBILE_MONEY);
        };

    HandlerTranRequest handler = new HandlerTranRequest();
    LocalDate posted =
        request.getPostedDate() != null
            ? request.getPostedDate()
            : eodDateService.getCurrentSystemDate(userShop.getShop());
    handler.setPostedDate(posted);
    handler.setTotalAmount(request.getAmount());
    handler.setDescription(request.getParticular().trim());
    handler.setCategory(categoryFor(request.getKind()));
    handler.setPrimaryAccountId(primaryShopAccount.getId());

    SecondaryTransactionRequest line = new SecondaryTransactionRequest();
    line.setShopAccountId(secondaryShopAccount.getId());
    line.setAmount(request.getAmount());
    line.setDescription(request.getParticular().trim());
    handler.setSecondaryTransactions(List.of(line));

    TranHeader header = tranHandlerService.createTransaction(handler);

    SimpleShopTransaction row =
        SimpleShopTransaction.builder()
            .shop(userShop.getShop())
            .postedBy(userShop)
            .kind(request.getKind())
            .paymentSource(request.getPaymentSource())
            .amount(request.getAmount())
            .particular(request.getParticular().trim())
            .postedDate(posted)
            .tranHeader(header)
            .build();
    SimpleShopTransaction saved = simpleShopTransactionRepository.save(row);

    return new SimpleTransactionPostResponse(
        saved.getId(),
        header.getTranId(),
        "Saved. Like other postings, this may still need supervisor verification.");
  }

  public List<SimpleTransactionListItem> listBetween(LocalDate from, LocalDate to) {
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("Invalid date range");
    }
    long shopId = authProvider.getCurrentUserShop().getShop().getId();
    return simpleShopTransactionRepository
        .findByShopAndPostedDateBetween(shopId, from, to)
        .stream()
        .map(
            s ->
                new SimpleTransactionListItem(
                    s.getId(),
                    s.getKind(),
                    s.getPaymentSource(),
                    s.getAmount(),
                    s.getParticular(),
                    s.getPostedDate().toString(),
                    s.getTranHeader().getTranId(),
                    s.getTranHeader().getStatus().name()))
        .toList();
  }

  private TransactionCategory categoryFor(SimpleTransactionKind kind) {
    return switch (kind) {
      case EXPENSE -> TransactionCategory.EXPENSE;
      case OWNERS_WITHDRAWAL -> TransactionCategory.OWNERS_DRAW;
      case CASH_OVERAGE -> TransactionCategory.OVERAGE;
      case CASH_SHORTAGE -> TransactionCategory.SHORTAGE;
    };
  }

  private DefaultAccount primaryDefault(SimpleTransactionKind kind) {
    return switch (kind) {
      case EXPENSE -> DefaultAccount.OTHER_EXPENSES;
      case OWNERS_WITHDRAWAL -> DefaultAccount.OWNER_DRAWINGS;
      case CASH_OVERAGE -> DefaultAccount.CASH_OVERAGE;
      case CASH_SHORTAGE -> DefaultAccount.CASH_SHORTAGE;
    };
  }

}
