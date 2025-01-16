package org.example.qposbackend.Order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.DTOs.ReturnItemRequest;
import org.example.qposbackend.EOD.EOD;
import org.example.qposbackend.EOD.EODRepository;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.OffersAndPromotions.Offers.OfferService;
import org.example.qposbackend.Order.OrderItem.OrderItem;
import org.example.qposbackend.Order.OrderItem.OrderItemRepository;
import org.example.qposbackend.Order.OrderItem.ReturnInward.ReturnInward;
import org.example.qposbackend.Order.OrderItem.ReturnInward.ReturnInwardRepository;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final AccountRepository accountRepository;
    private final TranHeaderService tranHeaderService;
    private final SpringSecurityAuditorAware auditorAware;
    private final OrderItemRepository orderItemRepository;
    private final ReturnInwardRepository returnInwardRepository;
    private final EODRepository eodRepository;
    private final OfferService offerService;

    public List<SaleOrder> fetchByDateRange(Date start, Date end) {
        List<SaleOrder> ordersWithinRange = orderRepository.fetchAllByDateRange(start, end);

        List<SaleOrder> returnedItems = orderRepository.fetchAllSalesReturnedWithinRange(start, end);
        returnedItems.forEach(order -> {
            order.setOrderItems(order.getOrderItems().stream().filter(item -> !Objects.isNull(item.getReturnInward())).toList());
        });

        ordersWithinRange.addAll(returnedItems);
        return ordersWithinRange;
    }

    @Transactional
    public void processOrder(SaleOrder saleOrder) {
        Date saleDate = getSaleDate();

        saleOrder.setOrderItems(
                saleOrder.getOrderItems()
                        .stream()
                        .peek((orderItem -> {
                            InventoryItem inventoryItem = inventoryItemRepository.findById(orderItem.getInventoryItem().getId()).orElseThrow(() -> new RuntimeException("No inventory found."));
                            if (orderItem.getDiscount() > inventoryItem.getDiscountAllowed()) {
                                throw new NotAcceptableException("Maximum discount for " + inventoryItem.getItem().getName() + " is " + inventoryItem.getDiscountAllowed());
                            }
                            orderItem.setInventoryItem(inventoryItem);
                        }))
                        .collect(Collectors.toCollection(ArrayList::new))
        );


        saleOrder = offerService.getOffersToApply(saleOrder).saleOrder(); //get the offers
        for (OrderItem orderItem : saleOrder.getOrderItems()) {
            InventoryItem inventoryItem = orderItem.getInventoryItem();
            inventoryItem.setQuantity(inventoryItem.getQuantity() - orderItem.getQuantity());
            inventoryItem = inventoryItemRepository.save(inventoryItem);
            orderItem.setInventoryItem(inventoryItem);
        }

        saleOrder.setDate(saleDate);
        tranHeaderService.saveAndVerifyTranHeader(makeSale(saleOrder));
        orderRepository.save(saleOrder);
    }

    @Transactional
    public void returnItem(ReturnItemRequest returnItemRequest) {
        OrderItem orderItem = orderItemRepository.findById(returnItemRequest.orderItemId()).orElseThrow(() -> new NoSuchElementException("No item found."));
        SaleOrder saleOrder = orderRepository.findByOrderItems(orderItem).orElseThrow(() -> new NoSuchElementException("Sale not found."));
        Date saleDate = getSaleDate();

        long dateDiff = ChronoUnit.DAYS.between(saleOrder.getDate().toInstant(), new Date().toInstant());
        if (Math.abs(dateDiff) > 30) {
            throw new NotAcceptableException("Items cannot be returned after 30 days.");
        } else if (orderItem.getQuantity() < returnItemRequest.quantity()) {
            throw new NotAcceptableException("Quantity returned must not exceed quantity was sold.");
        }


        if (Objects.isNull(orderItem.getReturnInward())) {

            ReturnInward returnInward = ReturnInward.builder()
                    .quantityReturned(returnItemRequest.quantity())
                    .dateReturned(saleDate)
                    .dateSold(saleOrder.getDate())
                    .returnReason(returnItemRequest.reason())
                    .costIncurred(returnItemRequest.chargesIncurred())
                    .build();
            returnInward = returnInwardRepository.save(returnInward);
            orderItem.setReturnInward(returnInward);

            orderItemRepository.save(orderItem);
            TranHeader tranHeader = returnItemTransactions(saleOrder, orderItem, returnItemRequest.quantity());
            tranHeaderService.saveAndVerifyTranHeader(tranHeader);
        } else {
            ReturnInward returnInward = orderItem.getReturnInward();
            returnInward.setDateReturned(saleDate);
            orderItemRepository.save(orderItem);
        }
    }

    private TranHeader returnItemTransactions(SaleOrder saleOrder, OrderItem orderItem, int quantity) {
        User user = auditorAware.getCurrentAuditor().orElseThrow(() -> new NoSuchElementException("User not found"));
//        Double amountSpent = (orderItem.getReturnInward().getQuantityReturned() * orderItem.getInventoryItem().getSellingPrice()) - orderItem.getDiscount();
        String accountName = "CASH";
        Account account = accountRepository.findByAccountName(accountName).orElseThrow(() -> new NoSuchElementException(accountName + " account not found."));
        Account costOfGoodsAccount = accountRepository.findByAccountName("COST OF GOODS").orElseThrow(() -> new NoSuchElementException("COST OF GOODS account not found"));
        Account salesAccount = accountRepository.findByAccountName("SALES REVENUE").orElseThrow(() -> new NoSuchElementException("SALES REVENUE account not found"));
        Account inventoryAccount = accountRepository.findByAccountName("INVENTORY").orElseThrow(() -> new NoSuchElementException("INVENTORY account not found"));

        TranHeader tranHeader = TranHeader.builder()
                .status(TransactionStatus.POSTED.name())
                .postedDate(getSaleDate())
                .postedBy(saleOrder.getCreatedBy())
                .verifiedBy(user)
                .status(TransactionStatus.UNVERIFIED.name())
                .build();
        List<PartTran> partTranList = new ArrayList<>();
        int partTranNumber = 1;

        for (int x = 0; x < quantity; x++) {
            PartTran tran = PartTran.builder()
                    .tranType('D')
                    .amount(orderItem.getInventoryItem().getBuyingPrice())
                    .tranParticulars("(sales) Returned  " + orderItem.getInventoryItem().getItem().getName())
                    .account(inventoryAccount)
                    .partTranNumber(partTranNumber++)
                    .build();
            partTranList.add(tran);

            tran = PartTran.builder()
                    .tranType('C')
                    .amount(orderItem.getInventoryItem().getBuyingPrice())
                    .tranParticulars("(sales) Cost of returned " + orderItem.getInventoryItem().getItem().getName())
                    .account(costOfGoodsAccount)
                    .partTranNumber(partTranNumber++)
                    .build();
            partTranList.add(tran);


            //Sales
            tran = PartTran.builder()
                    .tranType('D')
                    .amount(orderItem.getPrice() - orderItem.getDiscount())
                    .tranParticulars("(sales) Return of " + orderItem.getInventoryItem().getItem().getName())
                    .account(salesAccount)
                    .partTranNumber(partTranNumber++)
                    .build();
            partTranList.add(tran);

            tran = PartTran.builder()
                    .tranType('C')
                    .amount(orderItem.getPrice() - orderItem.getDiscount())
                    .tranParticulars("(sales) Return of " + orderItem.getInventoryItem().getItem().getName())
                    .account(account)
                    .partTranNumber(partTranNumber++)
                    .build();
            partTranList.add(tran);
        }

        tranHeader.setPartTrans(partTranList);
        return tranHeader;
    }

    private TranHeader makeSale(SaleOrder saleOrder) {

        User user = auditorAware.getCurrentAuditor().orElseThrow(() -> new NoSuchElementException("User not logged in"));

        TranHeader tranHeader = TranHeader.builder()
                .status(TransactionStatus.POSTED.name())
                .postedDate(getSaleDate())
                .postedBy(saleOrder.getCreatedBy())
                .verifiedBy(user)
                .status(TransactionStatus.UNVERIFIED.name())
                .build();

        Account cashAccount = accountRepository.findByAccountName("CASH").orElseThrow(() -> new NoSuchElementException("CASH account not found"));
        Account salesAccount = accountRepository.findByAccountName("SALES REVENUE").orElseThrow(() -> new NoSuchElementException("SALES REVENUE account not found"));
        Account costOfGoodsAccount = accountRepository.findByAccountName("COST OF GOODS").orElseThrow(() -> new NoSuchElementException("COST OF GOODS account not found"));
        Account inventoryAccount = accountRepository.findByAccountName("INVENTORY").orElseThrow(() -> new NoSuchElementException("INVENTORY account not found"));
        Account mobileMoneyAccount = accountRepository.findByAccountName("MOBILE MONEY").orElseThrow(() -> new NoSuchElementException("MOBILE MONEY account not found"));

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
                        .tranParticulars("(sales) Sold  " + orderItem.getInventoryItem().getItem().getName())
                        .account(inventoryAccount)
                        .partTranNumber(partTranNumber++)
                        .build();
                partTranList.add(tran);

                tran = PartTran.builder()
                        .tranType('D')
                        .amount(orderItem.getInventoryItem().getBuyingPrice())
                        .tranParticulars("(sales) Cost of " + orderItem.getInventoryItem().getItem().getName())
                        .account(costOfGoodsAccount)
                        .partTranNumber(partTranNumber++)
                        .build();
                partTranList.add(tran);


                //Sales
                tran = PartTran.builder()
                        .tranType('C')
                        .amount(orderItem.getPrice() - orderItem.getDiscount())
                        .tranParticulars("(sales) Sale of " + orderItem.getInventoryItem().getItem().getName())
                        .account(salesAccount)
                        .partTranNumber(partTranNumber++)
                        .build();
                partTranList.add(tran);

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
                            .tranParticulars("(sales) Sale of " + orderItem.getInventoryItem().getItem().getName())
                            .account(type)
                            .partTranNumber(partTranNumber++)
                            .build();
                    partTranList.add(tran);


                } else {
                    double cash = (orderItem.getPrice() - orderItem.getDiscount()) - amountInMobile;
                    tran = PartTran.builder()
                            .tranType('D')
                            .amount(cash)
                            .tranParticulars("(sales) Sale of " + orderItem.getInventoryItem().getItem().getName())
                            .account(cashAccount)
                            .partTranNumber(partTranNumber++)
                            .build();
                    partTranList.add(tran);

                    tran = PartTran.builder()
                            .tranType('D')
                            .amount(amountInMobile)
                            .tranParticulars("(sales) Sale of " + orderItem.getInventoryItem().getItem().getName())
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

    //    @Bean
    private void loadAllSalesInAccount() throws CloneNotSupportedException {
        Account salesAccount = accountRepository.findByAccountName("SALES REVENUE").orElseThrow(() -> new NoSuchElementException("SALES REVENUE account not found"));

        if (salesAccount.getBalance() != 0) {
            return;
        }
        List<SaleOrder> saleOrders = orderRepository.findAll();

        List<TranHeader> tranHeaders = new ArrayList<>();
        for (SaleOrder saleOrder : saleOrders) {
            TranHeader tranHeader = makeSale(saleOrder);
            tranHeaders.add(tranHeader);
        }

        try {
            tranHeaderService.verifyTransactions(tranHeaders);

        } catch (Exception ignored) {

        }

        log.info("All sales accounted for");
    }

    private Date getSaleDate() {
        Optional<EOD> eodOptional = eodRepository.findLastEOD();

        if (eodOptional.isPresent()) {
            EOD eod = eodOptional.get();
            long dateDiff = ChronoUnit.DAYS.between(LocalDate.now(), eod.getDate());
            if (dateDiff == 0) {
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DATE, 1);
                return tomorrow.getTime();
            } else if (Math.abs(dateDiff) > 1) {
                throw new NotAcceptableException("You cannot start sales before performing yesterday's End Of Day.");
            } else {
                return new Date();
            }
        } else {
            return new Date();
        }
    }
}
