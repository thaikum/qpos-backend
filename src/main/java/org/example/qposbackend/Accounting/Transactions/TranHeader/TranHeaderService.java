package org.example.qposbackend.Accounting.Transactions.TranHeader;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountRepository;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.PartTranDTO;
import org.example.qposbackend.DTOs.TranHeaderDTO;
import org.example.qposbackend.DTOs.TransactionDTO;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranHeaderService {
  private final TranHeaderRepository tranHeaderRepository;
  private final SpringSecurityAuditorAware springSecurityAuditorAware;
  private final SpringSecurityAuditorAware auditorAware;
  private final ShopAccountRepository shopAccountRepository;

  public void saveAndVerifyTranHeader(TranHeader tranHeader) {
    log.info("Before saving the transactions: {} ", tranHeader.toString());
    tranHeaderRepository.save(tranHeader);
    log.info("Saved the transactions");
    verifyTransaction(tranHeader);
  }

  public void verifyTransaction(TranHeader tranHeader) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    Map<Long, Double> shopAccountMap = new HashMap<>();

    List<Long> ids = new ArrayList<>();

    transactionProcessor(tranHeader, shopAccountMap, ids);
    tranHeaderRepository.verifyStatusByIds(userShop.getUser().getId(), ids);
    for (Map.Entry<Long, Double> entry : shopAccountMap.entrySet()) {
      shopAccountRepository.updateAccountBalance(entry.getKey(), entry.getValue());
    }
  }

  private void transactionProcessor(
      TranHeader tranHeader, Map<Long, Double> shopAccountMap, List<Long> ids) {
    Double net = 0.0;
    for (PartTran part : tranHeader.getPartTrans()) {
      ShopAccount shopAccount = part.getShopAccount();
      shopAccountMap.putIfAbsent(shopAccount.getId(), 0.0);
      Double change = shopAccountMap.get(shopAccount.getId());

      if (part.getTranType().equals('C')) {
        change += part.getAmount();
        net += part.getAmount();
      } else {
        change -= part.getAmount();
        net -= part.getAmount();
      }
      shopAccountMap.put(shopAccount.getId(), change);
    }

    if (net != 0.0) {
      throw new RuntimeException("Transaction must balance");
    }

    ids.add(tranHeader.getTranId());
  }

  @Transactional
  public void verifyTransactions(List<TranHeader> tranHeaders) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not logged in."));
    Map<Long, Double> shopAccountMap = new HashMap<>();

    List<Long> ids = new ArrayList<>();
    for (TranHeader tranHeader : tranHeaders) {
      transactionProcessor(tranHeader, shopAccountMap, ids);
    }
    log.info("Now saving");
    tranHeaderRepository.verifyStatusByIds(userShop.getUser().getId(), ids);
    for (Map.Entry<Long, Double> entry : shopAccountMap.entrySet()) {
      shopAccountRepository.updateAccountBalance(entry.getKey(), entry.getValue());
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
      UserShop userShop =
          springSecurityAuditorAware
              .getCurrentAuditor()
              .orElseThrow(() -> new NoSuchElementException("User not logged in"));

      TranHeader tranHeader =
          TranHeader.builder()
              .postedBy(userShop)
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
                .shopAccount(
                    shopAccountRepository
                        .findById(partTranDTO.shopAccountId())
                        .orElseThrow(() -> new NoSuchElementException("Account not found")))
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
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not logged in."));
    List<TranHeader> tranHeaders =
        tranHeaderRepository.findAllByStatusAndPostedDateBetween(
            userShop.getShop().getId(), status, range.start(), range.end());

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
                          : tranHeader.getLastModifiedBy().getUser().getEmail())
                  .accountName(
                      Objects.isNull(partTran.getShopAccount())
                          ? null
                          : partTran.getShopAccount().getAccount().getAccountName())
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
