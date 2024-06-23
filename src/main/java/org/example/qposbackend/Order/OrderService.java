package org.example.qposbackend.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderRepository;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.Order.OrderItem.OrderItem;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final AccountRepository accountRepository;
    private final TranHeaderService tranHeaderService;
    private final SpringSecurityAuditorAware auditorAware;
    private final TranHeaderRepository tranHeaderRepository;


    public void processOrder(SaleOrder saleOrder) {
        for (OrderItem orderItem : saleOrder.getOrderItems()) {
            InventoryItem inventoryItem = orderItem.getInventoryItem();
            inventoryItem.setQuantity(inventoryItem.getQuantity() - orderItem.getQuantity());
            inventoryItem = inventoryItemRepository.save(inventoryItem);
            orderItem.setInventoryItem(inventoryItem);
        }

//        makeSale(saleOrder);
        orderRepository.save(saleOrder);
    }

    private TranHeader makeSale(SaleOrder saleOrder) {

        User user = auditorAware.getCurrentAuditor().get();

        TranHeader tranHeader = TranHeader.builder()
                .status(TransactionStatus.POSTED.name())
                .postedDate(saleOrder.getDate())
                .postedBy(saleOrder.getCreatedBy())
                .verifiedBy(user)
                .status(TransactionStatus.UNVERIFIED.name())
                .build();

        Account cashAccount = accountRepository.findByAccountName("CASH").orElseThrow();
        Account salesAccount = accountRepository.findByAccountName("SALES REVENUE").orElseThrow();
        Account costOfGoodsAccount = accountRepository.findByAccountName("COST OF GOODS").orElseThrow();
        Account inventoryAccount = accountRepository.findByAccountName("INVENTORY").orElseThrow();
        Account mobileMoneyAccount = accountRepository.findByAccountName("MOBILE MONEY").orElseThrow();

        double amountInCash = Objects.isNull(saleOrder.getAmountInCash()) ? 0.0 : (saleOrder.getAmountInCash());
        double amountInMobile = Objects.isNull(saleOrder.getAmountInMpesa()) ? 0.0 : (saleOrder.getAmountInMpesa());

        if (amountInCash != 0) {
            amountInCash = saleOrder.getOrderItems().stream().mapToDouble(item -> (item.getPrice() - item.getDiscount()) * item.getQuantity()).sum() - amountInMobile;
        }

        List<PartTran> partTranList = new ArrayList<>();
        int partTranNumber = 1;
        for (OrderItem orderItem : saleOrder.getOrderItems()) {
            for (int x = 0; x < orderItem.getQuantity(); x++) {
                PartTran tran = PartTran.builder()
                        .tranType('C')
                        .amount(orderItem.getInventoryItem().getBuyingPrice())
                        .tranParticulars("Sold  " + orderItem.getInventoryItem().getItem().getName())
                        .account(inventoryAccount)
                        .partTranNumber(partTranNumber++)
                        .build();
                partTranList.add(tran);

                tran = PartTran.builder()
                        .tranType('D')
                        .amount(orderItem.getInventoryItem().getBuyingPrice())
                        .tranParticulars("Cost of " + orderItem.getInventoryItem().getItem().getName())
                        .account(costOfGoodsAccount)
                        .partTranNumber(partTranNumber++)
                        .build();
                partTranList.add(tran);


                //Sales
                tran = PartTran.builder()
                        .tranType('C')
                        .amount(orderItem.getPrice() - orderItem.getDiscount())
                        .tranParticulars("Sale of " + orderItem.getInventoryItem().getItem().getName())
                        .account(salesAccount)
                        .partTranNumber(partTranNumber++)
                        .build();
                partTranList.add(tran);

                Double amtT = tran.getAmount();

                Account type = null;
                if (amountInCash >= orderItem.getPrice() - orderItem.getDiscount()) {
                    type = cashAccount;
                    amountInCash -= (orderItem.getPrice() - orderItem.getDiscount());
                } else if (amountInMobile >= orderItem.getPrice() - orderItem.getDiscount()) {
                    type = mobileMoneyAccount;
                    amountInMobile -= (orderItem.getPrice() - orderItem.getDiscount());
                }

                if (type != null) {
                    tran = PartTran.builder()
                            .tranType('D')
                            .amount(orderItem.getPrice() - orderItem.getDiscount())
                            .tranParticulars("Sale of " + orderItem.getInventoryItem().getItem().getName())
                            .account(type)
                            .partTranNumber(partTranNumber++)
                            .build();
                    partTranList.add(tran);


                } else {
                    double cash = (orderItem.getPrice() - orderItem.getDiscount()) - amountInMobile;
                    tran = PartTran.builder()
                            .tranType('D')
                            .amount(cash)
                            .tranParticulars("Sale of " + orderItem.getInventoryItem().getItem().getName())
                            .account(cashAccount)
                            .partTranNumber(partTranNumber++)
                            .build();
                    partTranList.add(tran);

                    tran = PartTran.builder()
                            .tranType('D')
                            .amount(amountInMobile)
                            .tranParticulars("Sale of " + orderItem.getInventoryItem().getItem().getName())
                            .account(mobileMoneyAccount)
                            .partTranNumber(partTranNumber++)
                            .build();
                    partTranList.add(tran);

                    amountInMobile = 0.0;
                    amountInCash -= cash;
                }
            }
        }
        tranHeader.setPartTrans(partTranList);
        return tranHeader;
    }

    @Bean
    private void loadAllSalesInAccount() {
        Account salesAccount = accountRepository.findByAccountName("SALES REVENUE").orElseThrow();

        if (salesAccount.getBalance() != 0){
            return;
        }
        List<SaleOrder> saleOrders = orderRepository.findAll();

        List<TranHeader> tranHeaders = new ArrayList<>();
        for (SaleOrder saleOrder : saleOrders) {
            TranHeader tranHeader = makeSale(saleOrder);
            tranHeaders.add(tranHeader);
        }

        try{tranHeaderService.verifyTransactions(tranHeaders);

        }catch (Exception e){
            e.printStackTrace();
        }

        log.info("All sales accounted for");
    }
}
