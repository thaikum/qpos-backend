package org.example.qposbackend.Accounting.Transactions.TranHeader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.PartTranDTO;
import org.example.qposbackend.DTOs.TranHeaderDTO;
import org.example.qposbackend.DTOs.TransactionDTO;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranHeaderService {
  private final TranHeaderRepository tranHeaderRepository;
  private final SpringSecurityAuditorAware springSecurityAuditorAware;
  private final AccountRepository accountRepository;
  private final SpringSecurityAuditorAware auditorAware;

  public void saveAndVerifyTranHeader(TranHeader tranHeader) {
    tranHeaderRepository.save(tranHeader);
    verifyTransaction(tranHeader);
  }

  public void verifyTransaction(TranHeader tranHeader) {
    User user =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not logged in."));
    Map<String, Double> accountMap = new HashMap<>();

    List<Long> ids = new ArrayList<>();

    transactionProcessor(tranHeader, accountMap, ids);
    tranHeaderRepository.verifyStatusByIds(user.getId(), ids);
    for (Map.Entry<String, Double> entry : accountMap.entrySet()) {
      accountRepository.updateAccountBalance(entry.getKey(), entry.getValue());
    }
  }

  private void transactionProcessor(
      TranHeader tranHeader, Map<String, Double> accountMap, List<Long> ids) {
    Double net = 0.0;
    for (PartTran part : tranHeader.getPartTrans()) {
      Account account = part.getAccount();
      accountMap.putIfAbsent(account.getAccountNumber(), 0.0);
      Double change = accountMap.get(account.getAccountNumber());

      if (part.getTranType().equals('C')) {
        change += part.getAmount();
        net += part.getAmount();
      } else {
        change -= part.getAmount();
        net -= part.getAmount();
      }
      accountMap.put(account.getAccountNumber(), change);
    }

    if (net != 0.0) {
      throw new RuntimeException("Transaction must balance");
    }

    ids.add(tranHeader.getTranId());
  }

  @Transactional
  public void verifyTransactions(List<TranHeader> tranHeaders) {
    User user =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not logged in."));
    Map<String, Double> accountMap = new HashMap<>();

    List<Long> ids = new ArrayList<>();
    for (TranHeader tranHeader : tranHeaders) {
      transactionProcessor(tranHeader, accountMap, ids);
    }
    log.info("Now saving");
    tranHeaderRepository.verifyStatusByIds(user.getId(), ids);
    for (Map.Entry<String, Double> entry : accountMap.entrySet()) {
      accountRepository.updateAccountBalance(entry.getKey(), entry.getValue());
    }
    log.info("Done saving");
  }

  public void declineTranHeaders(List<Long> ids) {}

  public void createAndVerifyTransaction(TranHeaderDTO tranHeaderDTO) {
    TranHeader tranHeader = createTransactions(tranHeaderDTO);
    verifyTransaction(tranHeader);
  }

  public TranHeader createTransactions(TranHeaderDTO tranHeaderDTO) {
    try {
      User user =
          springSecurityAuditorAware
              .getCurrentAuditor()
              .orElseThrow(() -> new NoSuchElementException("User not logged in"));
      TranHeader tranHeader =
          TranHeader.builder()
              .postedBy(user)
              .postedDate(ObjectUtils.firstNonNull(tranHeaderDTO.postedDate(), new Date()))
              .status(TransactionStatus.UNVERIFIED.name())
              .build();

      List<PartTran> partTrans = new ArrayList<>();

      for (PartTranDTO partTranDTO : tranHeaderDTO.partTrans()) {
        PartTran partTran =
            PartTran.builder()
                .tranType(partTranDTO.tranType())
                .amount(partTranDTO.amount())
                .tranParticulars(partTranDTO.tranParticulars())
                .account(
                    accountRepository
                        .findByAccountNumber(partTranDTO.accountNumber())
                        .orElseThrow(
                            () ->
                                new NoSuchElementException(
                                    "Account with account number: "
                                        + partTranDTO.accountNumber()
                                        + " not found.")))
                .build();
        partTrans.add(partTran);
      }
      tranHeader.setPartTrans(partTrans);
      tranHeader.setStatus(TransactionStatus.UNVERIFIED.name());

      return tranHeaderRepository.save(tranHeader);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<TransactionDTO> fetchTransactionsByRange(DateRange range, String status) {
    List<TransactionDTO> transactionDTOList = new ArrayList<>();
    List<TranHeader> tranHeaders =
        tranHeaderRepository.findAllByStatusAndPostedDateBetween(
            status, range.start(), range.end());

    for (TranHeader tranHeader : tranHeaders) {
      List<PartTran> partTrans = tranHeader.getPartTrans();
      if (!Objects.isNull(partTrans) && !partTrans.isEmpty()) {
        for (PartTran partTran : partTrans) {
          TransactionDTO transactionDTO =
              TransactionDTO.builder()
                  .tranId(tranHeader.getTranId())
                  .tranDate(tranHeader.getCreationTimestamp())
                  .tranAmount(partTran.getAmount())
                  .tranStatus(tranHeader.getStatus())
                  .updatedBy(
                      Objects.isNull(tranHeader.getLastModifiedBy())
                          ? null
                          : tranHeader.getLastModifiedBy().getEmail())
                  .accountName(
                      Objects.isNull(partTran.getAccount())
                          ? null
                          : partTran.getAccount().getAccountName())
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
