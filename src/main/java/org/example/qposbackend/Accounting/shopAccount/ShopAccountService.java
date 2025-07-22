package org.example.qposbackend.Accounting.shopAccount;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Accounts.AccountTypes;
import org.example.qposbackend.Accounting.shopAccount.dto.ShopAccountDto;
import org.example.qposbackend.Accounting.shopAccount.mapper.Mapper;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.shop.Shop;
import org.example.qposbackend.shop.ShopRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopAccountService {
  private final ShopAccountRepository shopAccountRepository;
  private final AccountRepository accountRepository;
  private final SpringSecurityAuditorAware auditorAware;

  @Transactional
  public void createShopAccount(ShopAccountDto shopAccountDto) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    Account account;

    if (Objects.isNull(shopAccountDto.getId())) {
      int accounts =
          accountRepository.countByShopAndAccountType(
              userShop.getShop(), shopAccountDto.getAccountType());
      String accountNumber =
          String.format(
              "%02d%03d",
              AccountTypes.valueOf(shopAccountDto.getAccountType()).ordinal(), accounts + 1);
      account =
          Account.builder()
              .accountName(shopAccountDto.getAccountName())
              .accountNumber(accountNumber)
              .accountType(shopAccountDto.getAccountType())
              .description(shopAccountDto.getDescription())
              .build();
    } else {
      account =
          accountRepository
              .findById(shopAccountDto.getId())
              .orElseThrow(() -> new NoSuchElementException("Account not found"));
      if (shopAccountRepository.existsByShopAndAccount(userShop.getShop(), account)) {
        throw new IllegalStateException("Shop account already exists");
      }
    }

    ShopAccount shopAccount =
        ShopAccount.builder()
            .shop(userShop.getShop())
            .account(account)
            .balance(0.0)
            .currency(shopAccountDto.getCurrency())
            .isActive(true)
            .displayDescription(shopAccountDto.getDescription())
            .displayName(shopAccountDto.getAccountName())
            .build();

    shopAccountRepository.save(shopAccount);
  }

  public List<ShopAccountDto> getShopAccounts() {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    List<ShopAccount> accounts = shopAccountRepository.findAllByShop(userShop.getShop());
    log.info("Found {} accounts for shop {}", accounts.size(), userShop.getShop().getCode());
    return accounts.stream()
        .map((Mapper::shopAccountToShopAccountDto))
        .toList();
  }


  @Transactional
  public void updateBalance(Shop shop, Account account, Double amount) {
    shopAccountRepository.updateBalance(shop, account, amount);
  }

  public ShopAccountDto getShopAccount(Long id) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    ShopAccount shopAccount = shopAccountRepository
        .findById(id)
        .filter(sa -> sa.getShop().equals(userShop.getShop()))
        .orElseThrow(() -> new NoSuchElementException("Shop account not found"));
    Integer count = shopAccountRepository.countAllByAccount(shopAccount.getAccount());

    ShopAccountDto dto = Mapper.shopAccountToShopAccountDto(shopAccount);
    dto.setAccountTypeIsEditable(count == 1);
    return dto;
  }

  @Transactional
  public void toggleAccountStatus(Long id) {
//    ShopAccount shopAccount = getShopAccount(id);
//    shopAccount.setIsActive(!shopAccount.getIsActive());
//    shopAccountRepository.save(shopAccount);
  }

  @Bean
  private boolean migrateAllAccounts(
      ShopRepository shopRepository,
      AccountRepository accountRepository,
      ShopAccountRepository shopAccountRepository) {
    List<Shop> shops = shopRepository.findAll();
    List<Account> accounts = accountRepository.findAll();
    List<ShopAccount> shopAccounts = new ArrayList<>();
    for (Shop shop : shops) {
      for (Account account : accounts) {
        if (shopAccountRepository.existsByShopAndAccount(shop, account)) {
          continue;
        }

        ShopAccount shopAccount =
            ShopAccount.builder()
                .shop(shop)
                .account(account)
                .balance(account.getBalance() == null ? 0.0 : account.getBalance())
                .currency(account.getCurrency())
                .isActive(account.getIsActive() == null || account.getIsActive())
                .build();
        shopAccounts.add(shopAccount);
      }
    }
    shopAccountRepository.saveAll(shopAccounts);
    return true;
  }
}
