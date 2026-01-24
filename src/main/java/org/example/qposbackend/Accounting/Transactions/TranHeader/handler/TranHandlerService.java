package org.example.qposbackend.Accounting.Transactions.TranHeader.handler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderRepository;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.IStatisticsReport;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.TransactionsStatistics;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTranRequest;
import org.example.qposbackend.Accounting.Transactions.TranHeader.mappers.DataTransformers;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranHandlerService implements ITransactionHandler {
  private final Map<String, TransactionHandler> handlers = new HashMap<>();
  private final TranHeaderRepository tranHeaderRepository;
  private final AuthUserShopProvider authProvider;

  public TranHeader createTransaction(HandlerTranRequest request) {
    TransactionHandler handler = handlers.get(request.getCategory().name());
    if (handler == null) {
      throw new UnsupportedOperationException("Transaction category not found");
    }
    return handler.createAndPersistTranHeader(request);
  }

  public List<HandlerTran> getAllTransactionsByDate(
      LocalDate start,
      LocalDate endDate,
      Optional<TransactionCategory> category,
      TransactionStatus status) {
    Shop shop = authProvider.getCurrentShop();
    String cat = category.map(Enum::name).orElse(null);
    List<TranHeader> transactions =
        tranHeaderRepository.findTransactionsCustom(
            shop.getId(), status.name(), start, endDate, cat);

    return transactions.stream().map(DataTransformers::tranHeaderToHandlerTran).toList();
  }

  public TransactionsStatistics getStatistics(
      LocalDate start,
      LocalDate endDate,
      Optional<TransactionCategory> category) {

    Shop shop = authProvider.getCurrentShop();
    String cat = category.map(Enum::name).orElse(null);
    List<IStatisticsReport> statistics =
        tranHeaderRepository.getTransactionStatistics(
            shop.getId(), start, endDate, cat);

    TransactionsStatistics stat = new TransactionsStatistics();
    for (IStatisticsReport report : statistics) {
      log.info("Report: {}, {}", report.getStatus(), report.getTotalCount());
      switch (report.getStatus()) {
        case VERIFIED -> stat.setTotalVerified(report.getTotalCount());
        case UNVERIFIED -> stat.setTotalUnverified(report.getTotalCount());
        case DECLINED -> stat.setTotalDeclined(report.getTotalCount());
        case POSTED -> stat.setTotalPosted(report.getTotalCount());
        default -> throw new IllegalStateException("Unexpected value: " + report.getStatus());
      }
    }
    return stat;
  }

  @Override
  public void register(TransactionCategory category, TransactionHandler handler) {
    handlers.put(category.name(), handler);
  }
}
