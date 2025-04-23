package org.example.qposbackend.EOD;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTranRepository;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.DTOs.EndOfDayDTO;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.example.qposbackend.Order.OrderService;
import org.example.qposbackend.Order.SaleOrder;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EODService {
  private final OrderService orderService;
  private final EODRepository eoDRepository;
  private final PartTranRepository partTranRepository;
  private final SpringSecurityAuditorAware auditorAware;
  private List<PartTran> eodTrasactions = new ArrayList<>();

  /**
   * \ Fix with values Expected: 2873 Available: 3280 Cash: 2835 Mpesa: 445
   *
   * @param endOfDayDTO
   */
  public void performEndOfDay(EndOfDayDTO endOfDayDTO) {
    eodTrasactions = new ArrayList<>();
    Optional<EOD> previousEodOpt = eoDRepository.findLastEOD();

    CurAssets totalSales = getTodaySales(previousEodOpt.orElse(EOD.builder().date(LocalDate.now()).build()).getDate().plusDays(1));
    double totalCashSale = totalSales.cashTotal;
    double totalMobileSale = totalSales.mobileTotal;
    double totalReceivables =
        endOfDayDTO.totalRecoveredDebt()
            + endOfDayDTO.balanceBroughtDownCash()
            + endOfDayDTO.balanceBroughtDownMobile();
    User user =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    EOD eod =
        EOD.builder()
            .date(LocalDate.now())
            .balanceBroughtDownCash(endOfDayDTO.balanceBroughtDownCash())
            .totalDebtors(totalSales.debtTotal)
            .balanceBroughtDownMobile(endOfDayDTO.balanceBroughtDownMobile())
            .totalCashSale(totalCashSale)
            .totalMobileSale(totalMobileSale)
            .user(user)
            .build();

    if (previousEodOpt.isPresent()) {
      EOD previousEod = previousEodOpt.get();

      log.info("EOD previousEod: {}", previousEod);
      long dateDiff = ChronoUnit.DAYS.between(LocalDate.now(), previousEod.getDate());

      if (dateDiff == 0) {
        throw new NotAcceptableException("End of Day cannot be done twice in the same day");
      } else {
        eod.setDate(previousEod.getDate().plusDays(1));
      }

      double previousDayTotal = getPreviousDayTotal(endOfDayDTO, previousEod);
      CurAssets allTransactions = getCashAndMobileDebits(eod.getDate());

      double totalAmount = allTransactions.cashTotal + allTransactions.mobileTotal;
      System.out.println("Non sale are: " + totalAmount);

      double expectedTotal =
          previousDayTotal + totalAmount;

      // update total debtors
      double previousDebt = Optional.ofNullable(previousEod.getTotalDebtors()).orElse(0D);
      eod.setTotalDebtors(previousDebt - endOfDayDTO.totalRecoveredDebt() + totalSales.debtTotal);
      double mpesaCost = totalMobileSale * 0.50 / 100 * 2;

      if (Math.abs(expectedTotal - totalReceivables) > mpesaCost) {
        throw new NotAcceptableException(
            """
                        Transactions failed to balance. <br/>
                        Expected: <b>Sh %.2f</b> <br/>
                        Available: <b>Sh %.2f</b> <br/>
                        Difference: <b>Sh %.2f</b> <br/>
                        """
                .formatted(expectedTotal, totalReceivables, expectedTotal - totalReceivables));
      }
    }

    eod.setInvolvedTransactions(eodTrasactions);
    eoDRepository.save(eod);
  }

  private double getPreviousDayTotal(EndOfDayDTO endOfDayDTO, EOD previousEod) {
    double previousDebt = Optional.ofNullable(previousEod.getTotalDebtors()).orElse(0D);
    System.out.println(
        "PreviousDebt: " + previousDebt + " Recovered Debt: " + endOfDayDTO.totalRecoveredDebt());
    if (previousDebt < endOfDayDTO.totalRecoveredDebt()) {
      throw new NotAcceptableException(
          "You cannot recover a debt that was not owed. There is no history any debt amounting to "
              + endOfDayDTO.totalRecoveredDebt());
    }

    return previousEod.getBalanceBroughtDownCash() + previousEod.getBalanceBroughtDownMobile();
  }

  private CurAssets getCashAndMobileDebits(LocalDate localDate) {
    List<PartTran> cashTransactions =
        partTranRepository.findAllVerifiedByVerifiedDateBetweenAndAccountName(
            localDate, LocalDate.now(), "CASH");

    for (PartTran partTran : cashTransactions) {
      log.info("Part tran: {}", partTran);
    }
    eodTrasactions.addAll(cashTransactions);

    List<PartTran> mobileMoneyTransactions =
        partTranRepository.findAllVerifiedByVerifiedDateBetweenAndAccountName(
            localDate, LocalDate.now(), "MOBILE MONEY");


    eodTrasactions.addAll(mobileMoneyTransactions);

    double totalCashTransactions =
        cashTransactions.stream()
            .mapToDouble(
                (partTran) ->
                    partTran.getTranType() == 'D' ? partTran.getAmount() : -partTran.getAmount())
            .sum();
    double totalMobileMoneyTransactions =
        mobileMoneyTransactions.stream()
            .mapToDouble(
                (partTran) ->
                    partTran.getTranType() == 'D' ? partTran.getAmount() : -partTran.getAmount())
            .sum();
    ;

    return new CurAssets(totalCashTransactions, totalMobileMoneyTransactions, null);
  }

  private CurAssets getTodaySales(LocalDate localDate) {
    Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    List<SaleOrder> sales = orderService.fetchByDateRange(date, date);

    double totalCashSale = 0D;
    double totalMobileSale = 0D;
    double totalDebtOwed = 0D;

    for (SaleOrder saleOrder : sales) {
      double cash = Optional.ofNullable(saleOrder.getAmountInCash()).orElse(0D);
      double mobileMoney = Optional.ofNullable(saleOrder.getAmountInMpesa()).orElse(0D);
      totalDebtOwed += Optional.ofNullable(saleOrder.getAmountInCredit()).orElse(0D);

      double sale =
          saleOrder.getOrderItems().stream()
                  .mapToDouble(
                      (orderItem) -> {
                        double discount =
                            Optional.ofNullable(orderItem.getInventoryItem().getDiscountAllowed())
                                        .orElse(0D)
                                    > 0D
                                ? orderItem.getInventoryItem().getDiscountAllowed()
                                : 0;

                        if (Objects.isNull(orderItem.getReturnInward())) {
                          return orderItem.getQuantity() * (orderItem.getPrice() - discount);
                        } else {
                          return -(orderItem.getReturnInward().getQuantityReturned()
                              * (orderItem.getPrice() - discount)); // subtract all returned goods.
                        }
                      })
                  .sum()
              - totalDebtOwed;

      if (cash >= sale) {
        totalCashSale += sale;
      } else if (mobileMoney >= sale) {
        totalMobileSale += sale;
      } else {
        totalMobileSale += mobileMoney;
        if (mobileMoney < sale) {
          totalCashSale += (sale - mobileMoney);
        }
      }
    }
    return new CurAssets(totalCashSale, totalMobileSale, totalDebtOwed);
  }

  record CurAssets(Double cashTotal, Double mobileTotal, Double debtTotal) {}
}
