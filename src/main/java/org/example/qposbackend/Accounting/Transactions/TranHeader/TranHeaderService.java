package org.example.qposbackend.Accounting.Transactions.TranHeader;

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
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
//@Transactional
public class TranHeaderService {
    private final TranHeaderRepository tranHeaderRepository;
    private final PartTranRepository partTranRepository;
    private final SpringSecurityAuditorAware springSecurityAuditorAware;
    private final AccountRepository accountRepository;


    public void processTransaction(TranHeader tranHeader) {
        double amount = 0.0, net = 0.0;

        for (PartTran partTran : tranHeader.getPartTrans()) {
            amount += partTran.getTranType().equals('C') ? partTran.getAmount() : 0.0;

            Account account = partTran.getAccount();
            if (partTran.getTranType().equals('C')) {
                net -= partTran.getAmount();

                account.setBalance(account.getBalance() - partTran.getAmount());

            } else {
                net += partTran.getAmount();
                account.setBalance(account.getBalance() + partTran.getAmount());
            }
            account = accountRepository.save(account);
            partTran.setAccount(account);

            log.info("{}", partTran);
        }

        if (net != 0.0) {
            throw new RuntimeException("Transaction must balance");
        } else {
            List<PartTran> partTrans = partTranRepository.saveAll(tranHeader.getPartTrans());
            tranHeader.setPartTrans(partTrans);
            tranHeaderRepository.save(tranHeader);
        }
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

            processTransaction(tranHeader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<TransactionDTO> fetchTransactions() {
        List<TransactionDTO> transactionDTOList = new ArrayList<>();
        List<TranHeader> tranHeaders = tranHeaderRepository.findAll();

        for (TranHeader tranHeader : tranHeaders) {
            List<PartTran> partTrans = tranHeader.getPartTrans();
            if(!Objects.isNull(partTrans) && !partTrans.isEmpty()) {
                for (PartTran partTran : partTrans) {
                    TransactionDTO transactionDTO = TransactionDTO.builder()
                            .tranId(tranHeader.getTranId())
                            .tranDate(tranHeader.getCreationTimestamp())
                            .tranAmount(partTran.getAmount())
                            .tranStatus(tranHeader.getStatus())
                            .updatedBy(Objects.isNull(partTran.getLastModifiedBy()) ? null : partTran.getLastModifiedBy().getEmail())
                            .accountName(Objects.isNull(partTran.getAccount())? null : partTran.getAccount().getAccountName())
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
