package org.example.qposbackend.Accounting.Transactions.TranHeader;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTranRepository;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.DTOs.PartTranDTO;
import org.example.qposbackend.DTOs.TranHeaderDTO;
import org.example.qposbackend.DTOs.TransactionDTO;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
//@Transactional
public class TranHeaderService {
    private final TranHeaderRepository tranHeaderRepository;
    private final PartTranRepository partTranRepository;
    private final SpringSecurityAuditorAware springSecurityAuditorAware;
    private final AccountRepository accountRepository;
    private final SpringSecurityAuditorAware auditorAware;
    private final EntityManager entityManager;

    //    @Transactional
    public void verifyTransaction(TranHeader tranHeader) {
        AtomicReference<Double> net = new AtomicReference<>(0.0);
        User user = auditorAware.getCurrentAuditor().get();

        tranHeader.getPartTrans().forEach(partTran -> {
            Account account = partTran.getAccount();
            if (partTran.getTranType().equals('C')) {
                net.updateAndGet(v -> (v - partTran.getAmount()));
                account.setBalance(account.getBalance() - partTran.getAmount());
            } else {
                net.updateAndGet(v -> (v + partTran.getAmount()));
                account.setBalance(account.getBalance() + partTran.getAmount());
            }

            account = accountRepository.save(account);
            partTran.setAccount(account);

            log.info("{}", partTran);
        });

        if (net.get() != 0.0) {
            throw new RuntimeException("Transaction must balance");
        } else {
            tranHeader.setVerifiedBy(user);
            tranHeader.setVerifiedDate(new Date());
            tranHeaderRepository.save(tranHeader);
        }
    }

    //    @Transactional(rollbackFor = Exception.class)
//    @Transactional
    public void verifyTransactions(List<TranHeader> tranHeaders) {
        User user = auditorAware.getCurrentAuditor().get();

        for(TranHeader tranHeader : tranHeaders) {
            tranHeader.setVerifiedBy(user);

            for(PartTran part : tranHeader.getPartTrans()) {
                Account account = part.getAccount();
                if (part.getTranType().equals('C')) {
                    account.setBalance(account.getBalance() - part.getAmount());
                }else{
                    account.setBalance(account.getBalance() + part.getAmount());
                }
                account = accountRepository.save(account);
                part.setAccount(account);
            }
        }
        log.info("Now saving");
        tranHeaderRepository.saveAll(tranHeaders);
        log.info("Done saving");
    }


    public void createTransactions(TranHeaderDTO tranHeaderDTO) {
        try {
            User user = springSecurityAuditorAware.getCurrentAuditor().get();
            TranHeader tranHeader = TranHeader.builder()
                    .postedBy(user)
                    .postedDate(tranHeaderDTO.postedDate())
                    .status(TransactionStatus.UNVERIFIED.name())
                    .build();

            List<PartTran> partTrans = new ArrayList<>();

            for (PartTranDTO partTranDTO : tranHeaderDTO.partTrans()) {
                PartTran partTran = PartTran.builder()
                        .tranType(partTranDTO.tranType())
                        .amount(partTranDTO.amount())
                        .tranParticulars(partTranDTO.tranParticulars())
                        .account(accountRepository.findByAccountNumber(partTranDTO.accountNumber()).get())
                        .build();
                partTrans.add(partTran);
            }
            tranHeader.setPartTrans(partTrans);

            verifyTransaction(tranHeader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<TransactionDTO> fetchTransactions() {
        List<TransactionDTO> transactionDTOList = new ArrayList<>();
        List<TranHeader> tranHeaders = tranHeaderRepository.findAll();

        for (TranHeader tranHeader : tranHeaders) {
            List<PartTran> partTrans = tranHeader.getPartTrans();
            if (!Objects.isNull(partTrans) && !partTrans.isEmpty()) {
                for (PartTran partTran : partTrans) {
                    TransactionDTO transactionDTO = TransactionDTO.builder()
                            .tranId(tranHeader.getTranId())
                            .tranDate(tranHeader.getCreationTimestamp())
                            .tranAmount(partTran.getAmount())
                            .tranStatus(tranHeader.getStatus())
                            .updatedBy(Objects.isNull(tranHeader.getLastModifiedBy()) ? null : tranHeader.getLastModifiedBy().getEmail())
                            .accountName(Objects.isNull(partTran.getAccount()) ? null : partTran.getAccount().getAccountName())
                            .tranType(partTran.getTranType())
                            .tranParticulars(partTran.getTranParticulars())
                            .build();

                    transactionDTOList.add(transactionDTO);
                }
            }
        }
        return transactionDTOList;
    }
}
