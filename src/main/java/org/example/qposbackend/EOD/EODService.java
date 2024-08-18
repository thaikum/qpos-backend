package org.example.qposbackend.EOD;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTranRepository;
import org.example.qposbackend.DTOs.EndOfDayDTO;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.example.qposbackend.Order.OrderService;
import org.example.qposbackend.Order.SaleOrder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EODService {
    private final OrderService orderService;
    private final EODRepository eoDRepository;
    private final PartTranRepository partTranRepository;

    public void performEndOfDay(EndOfDayDTO endOfDayDTO) {
        CurAssets totalSales = getTodaySales();
        double totalCashSale = totalSales.cashTotal;
        double totalMobileSale = totalSales.mobileTotal;
        double totalReceivables = endOfDayDTO.totalRecoveredDebt() + endOfDayDTO.balanceBroughtDownCash() + endOfDayDTO.balanceBroughtDownMobile();

        Optional<EOD> previousEodOpt = eoDRepository.findLastEOD();
        EOD eod = EOD.builder().date(new Date()).balanceBroughtDownCash(endOfDayDTO.balanceBroughtDownCash()).totalDebtors(totalSales.debtTotal).balanceBroughtDownMobile(endOfDayDTO.balanceBroughtDownMobile()).totalCashSale(totalCashSale).totalMobileSale(totalMobileSale).build();

        if (previousEodOpt.isPresent()) {
            EOD previousEod = previousEodOpt.get();
            long dateDiff = ChronoUnit.DAYS.between(new Date().toInstant(), eod.getDate().toInstant());

            if(dateDiff == 0){
                throw new NotAcceptableException("End of Day cannot be done twice in the same day");
            }

            double previousDayTotal = getPreviousDayTotal(endOfDayDTO, previousEod);
            CurAssets nonSaleTotals = getCashAndMobileDebits();
            double nonSaleTransactions = nonSaleTotals.cashTotal + nonSaleTotals.mobileTotal;
            double expectedTotal = previousDayTotal + nonSaleTransactions + totalCashSale + totalMobileSale;

            //update total debtors
            double previousDebt = Objects.isNull(previousEod.getTotalDebtors()) ? 0 : previousEod.getTotalDebtors();
            eod.setTotalDebtors(previousDebt - endOfDayDTO.totalRecoveredDebt() + totalSales.debtTotal);

            if (expectedTotal != totalReceivables) {
                throw new NotAcceptableException("The expected amount and current amount do not match! There is a difference of " + (expectedTotal - totalReceivables) + ". Make sure all sales and non-sale transactions are well recorded and verified.");
            }
        }
        eoDRepository.save(eod);
    }

    private double getPreviousDayTotal(EndOfDayDTO endOfDayDTO, EOD previousEod) {
        double previousDebt = Objects.isNull(previousEod.getTotalDebtors()) ? 0 : previousEod.getTotalDebtors();
        System.out.println("PreviousDebt: " + previousDebt + " Recovered Debt: " + endOfDayDTO.totalRecoveredDebt());
        if (previousDebt < endOfDayDTO.totalRecoveredDebt()) {
            throw new NotAcceptableException("You cannot recover a debt that was not owed. There is no history any debt amounting to " + endOfDayDTO.totalRecoveredDebt());
        }

        return previousEod.getBalanceBroughtDownCash() + previousEod.getBalanceBroughtDownMobile();
    }

    private CurAssets getTodaySales() {
        List<SaleOrder> sales = orderService.fetchByDateRange(new Date(), new Date());

        double totalCashSale = 0D;
        double totalMobileSale = 0D;
        double totalDebtOwed = 0D;

        for (SaleOrder saleOrder : sales) {
            Double cash = saleOrder.getAmountInCash();
            Double mobileMoney = saleOrder.getAmountInMpesa();
            totalDebtOwed += saleOrder.getAmountInCredit();

            double sale = saleOrder.getOrderItems().stream().mapToDouble((order) -> {
                if (Objects.isNull(order.getReturnInward())) {
                    return order.getQuantity() * (order.getPrice() - order.getDiscount());
                } else {
                    return -(order.getReturnInward().getQuantityReturned() * (order.getPrice() - order.getDiscount())); //subtract all returned goods.
                }
            }).sum() - totalDebtOwed;

            if (!Objects.isNull(cash) && !Objects.isNull(mobileMoney) && cash > 0 && mobileMoney > 0) {
                totalMobileSale += mobileMoney;
                totalCashSale += sale - mobileMoney;
            } else {
                if (!Objects.isNull(cash) && cash > 0) {
                    totalCashSale += sale;
                } else {
                    totalMobileSale += sale;
                }
            }
        }

        return new CurAssets(totalCashSale, totalMobileSale, totalDebtOwed);
    }

    private CurAssets getCashAndMobileDebits() {
        List<PartTran> cashTransactions = partTranRepository.findAllVerifiedByVerifiedDateBetweenAndAccountName(new Date(), new Date(), "CASH");
        List<PartTran> mobileMoneyTransactions = partTranRepository.findAllVerifiedByVerifiedDateBetweenAndAccountName(new Date(), new Date(), "MOBILE MONEY");

        double totalNonSaleCash = cashTransactions.stream().filter(partTran -> !partTran.getTranParticulars().contains("(sales)")).mapToDouble((partTran) -> partTran.getTranType() == 'C' ? partTran.getAmount() : -partTran.getAmount()).sum();
        double totalNonSaleMobile = mobileMoneyTransactions.stream().filter(partTran -> !partTran.getTranParticulars().contains("(sales)")).mapToDouble((partTran) -> partTran.getTranType() == 'C' ? partTran.getAmount() : -partTran.getAmount()).sum();
        ;

        return new CurAssets(totalNonSaleCash, totalNonSaleMobile, null);
    }

    record CurAssets(Double cashTotal, Double mobileTotal, Double debtTotal) {
    }
}
