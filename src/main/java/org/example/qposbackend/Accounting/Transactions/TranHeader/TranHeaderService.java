package org.example.qposbackend.Accounting.Transactions.TranHeader;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.SimpleJournal;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.TranHeaderResponseDTO;
import org.example.qposbackend.Accounting.Transactions.TranHeader.mappers.TranHeaderMapper;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountRepository;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.PartTranDTO;
import org.example.qposbackend.DTOs.TranHeaderDTO;
import org.example.qposbackend.DTOs.TransactionDTO;
import org.example.qposbackend.EOD.EODDateService;
import org.example.qposbackend.EOD.EODService;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.qposbackend.constants.Constants.TIME_ZONE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranHeaderService {
  private final TranHeaderRepository tranHeaderRepository;
  private final SpringSecurityAuditorAware springSecurityAuditorAware;
  private final SpringSecurityAuditorAware auditorAware;
  private final ShopAccountRepository shopAccountRepository;
  private final AuthUserShopProvider authProvider;
  private final EODDateService dateService;

  public TranHeader createSimpleTransaction(SimpleJournal simpleJournal) {
    UserShop userShop = authProvider.getCurrentUserShop();
    List<PartTran> partTrans = new ArrayList<>();

    int x = 0;
    for (var jLine : simpleJournal.getJournalLines()) {
      ShopAccount shopAccount =
          shopAccountRepository
              .findByAccountNameAndShop(jLine.getAccountName(), userShop.getShop())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          String.format(
                              "Account with name %s does not exist!", jLine.getAccountName())));
      partTrans.add(
          PartTran.builder()
              .partTranNumber(x)
              .tranType(jLine.getTranType())
              .amount(jLine.getAmount())
              .tranParticulars(simpleJournal.getParticulars())
              .shopAccount(shopAccount)
              .build());
      x++;
    }

    TranHeader tranHeader =
        TranHeader.builder()
            .status(TransactionStatus.UNVERIFIED)
            .postedDate(dateService.getCurrentSystemDate(userShop.getShop()))
            .postedBy(userShop)
            .partTrans(partTrans)
            .build();

    return saveVerifyAndReturn(tranHeader);
  }

  public void saveAndVerifyTranHeader(TranHeader tranHeader) {
    tranHeaderRepository.save(tranHeader);
    verifyTransaction(tranHeader);
  }

  public TranHeader saveVerifyAndReturn(TranHeader tranHeader) {
    tranHeader = tranHeaderRepository.save(tranHeader);
    return verifyTranAndReturn(tranHeader);
  }

  public void verifyTransaction(TranHeader tranHeader) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    Map<Long, Double> shopAccountMap = new HashMap<>();

    List<Long> ids = new ArrayList<>();

    processTransaction(tranHeader, shopAccountMap, ids);
    tranHeaderRepository.verifyStatusByIds(userShop.getId(), ids);
    for (Map.Entry<Long, Double> entry : shopAccountMap.entrySet()) {
      shopAccountRepository.updateAccountBalance(entry.getKey(), entry.getValue());
    }
  }

  private void processTransaction(
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
      processTransaction(tranHeader, shopAccountMap, ids);
    }
    log.info("Now saving");
    tranHeaderRepository.verifyStatusByIds(userShop.getId(), ids);
    for (Map.Entry<Long, Double> entry : shopAccountMap.entrySet()) {
      shopAccountRepository.updateAccountBalance(entry.getKey(), entry.getValue());
    }
    log.info("Done saving");
  }

  public TranHeader verifyTranAndReturn(TranHeader tranHeader) {
    tranHeader.setStatus(TransactionStatus.VERIFIED);
    tranHeader.setVerifiedBy(authProvider.getCurrentUserShop());
    List<Long> ids = new ArrayList<>();
    Map<Long, Double> shopAccountMap = new HashMap<>();

    processTransaction(tranHeader, shopAccountMap, ids);
    tranHeader = tranHeaderRepository.save(tranHeader);

    for (Map.Entry<Long, Double> entry : shopAccountMap.entrySet()) {
      shopAccountRepository.updateAccountBalance(entry.getKey(), entry.getValue());
    }
    return tranHeader;
  }

  public void declineTransactions(List<Long> idList) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not logged in."));

    List<TranHeader> tranHeaders = tranHeaderRepository.findAllById(idList);
    tranHeaders.forEach(
        tranHeader -> {
          tranHeader.setStatus(TransactionStatus.DECLINED);
          tranHeader.setRejectedDate(LocalDate.now(ZoneId.of(TIME_ZONE)));
          tranHeader.setRejectedBy(userShop);
        });
    tranHeaderRepository.saveAll(tranHeaders);
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
              .postedDate(
                  Objects.requireNonNullElse(
                      tranHeaderDTO.postedDate(),
                      dateService.getCurrentSystemDate(userShop.getShop())))
              .status(TransactionStatus.UNVERIFIED)
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
      tranHeader.setStatus(TransactionStatus.UNVERIFIED);

      return tranHeaderRepository.save(tranHeader);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<TranHeaderResponseDTO> fetchTransactionsByRange(DateRange range, String status) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not logged in."));
    List<TranHeader> tranHeaders =
        tranHeaderRepository.findAllByStatusAndPostedDateBetween(
            userShop.getShop().getId(), status, range.start(), range.end());

    return tranHeaders.stream()
        .map(TranHeaderMapper::toResponseDTO)
        .toList();
  }

  public TranHeader createBaseTranHeader(LocalDate date, UserShop userShop) {
    return TranHeader.builder()
        .postedDate(date)
        .postedBy(userShop)
        .verifiedBy(userShop)
        .status(TransactionStatus.VERIFIED)
        .verifiedDate(date)
        .build();
  }
}
