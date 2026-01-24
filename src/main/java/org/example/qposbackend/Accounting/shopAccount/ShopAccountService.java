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
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.shop.Shop;
import org.example.qposbackend.shop.ShopRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.lang.model.type.NullType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopAccountService {
  private final ShopAccountRepository shopAccountRepository;
  private final AccountRepository accountRepository;
  private final SpringSecurityAuditorAware auditorAware;
  private final AuthUserShopProvider authProvider;

  @Transactional
  public void createShopAccount(ShopAccount shopAccountDto) {
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

      ShopAccount shopAccount =
          ShopAccount.builder()
              .shop(userShop.getShop())
              .accountName(shopAccountDto.getAccountName())
              .accountNumber(accountNumber)
              .accountType(shopAccountDto.getAccountType())
              .balance(0.0)
              .currency(shopAccountDto.getCurrency())
              .isActive(true)
              .description(shopAccountDto.getDescription())
              .build();

      shopAccountRepository.save(shopAccount);
    } else {
      account =
          accountRepository
              .findById(shopAccountDto.getId())
              .orElseThrow(() -> new NoSuchElementException("Account not found"));
      if (shopAccountRepository.existsByShopAndAccount(userShop.getShop(), account)) {
        throw new IllegalStateException("Shop account already exists");
      }
    }
  }

  @Bean
  private NullType mapAccountToShopAccount(ShopAccountRepository shopAccountRepository) {
    List<ShopAccount> shopAccounts = shopAccountRepository.findAllByAccountNameIsNull();
    for (ShopAccount shopAccount : shopAccounts) {
      if (shopAccount.getAccount() != null) {
        shopAccount.setAccountNumber(shopAccount.getAccount().getAccountNumber());
        shopAccount.setAccountName(shopAccount.getAccount().getAccountName());
        shopAccount.setAccountType(shopAccount.getAccount().getAccountType());
        shopAccount.setDescription(shopAccount.getAccount().getDescription());
      }
    }
    shopAccountRepository.saveAll(shopAccounts);
    return null;
  }

  public List<ShopAccount> getShopAccounts() {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    List<ShopAccount> accounts = shopAccountRepository.findAllByShop(userShop.getShop());
    log.info("Found {} accounts for shop {}", accounts.size(), userShop.getShop().getCode());
    return accounts;
  }

  @Transactional
  public void updateBalance(Shop shop, Account account, Double amount) {
    shopAccountRepository.updateBalance(shop, account, amount);
  }

  public ShopAccount getShopAccountById(Long id){
    return shopAccountRepository.findById(id).orElseThrow(()->new NoSuchElementException("Shop account not found"));
  }

  public ShopAccountDto getShopAccount(Long id) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    ShopAccount shopAccount =
        shopAccountRepository
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

  public ShopAccountDto updateAccount(Long id, ShopAccountDto shopAccountDto) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    ShopAccount oldShopAccount =
        shopAccountRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Shop account not found"));

    if (!Objects.equals(oldShopAccount.getShop().getId(), userShop.getShop().getId())) {
      throw new IllegalStateException("Shop account does not belong to the current user");
    }

    if (!oldShopAccount.getAccount().getAccountName().equals(shopAccountDto.getAccountName())
        && shopAccountDto.getAccountName() != null) {
      oldShopAccount.setAccountName(shopAccountDto.getAccountName());
    }
    oldShopAccount.setDescription(
        ObjectUtils.firstNonNull(oldShopAccount.getDescription(), shopAccountDto.getDescription()));
    oldShopAccount.setCurrency(
        ObjectUtils.firstNonNull(shopAccountDto.getCurrency(), oldShopAccount.getCurrency()));
    oldShopAccount.setBalance(
        ObjectUtils.firstNonNull(shopAccountDto.getBalance(), oldShopAccount.getBalance()));
    int count = shopAccountRepository.countAllByAccount(oldShopAccount.getAccount());
    if (count == 1) {
      oldShopAccount
          .getAccount()
          .setAccountType(
              ObjectUtils.firstNonNull(
                  shopAccountDto.getAccountType(), oldShopAccount.getAccount().getAccountType()));
      oldShopAccount
          .getAccount()
          .setAccountName(
              ObjectUtils.firstNonNull(
                  shopAccountDto.getAccountName(), oldShopAccount.getAccount().getAccountName()));
    }
    oldShopAccount = shopAccountRepository.save(oldShopAccount);
    return Mapper.shopAccountToShopAccountDto(oldShopAccount);
  }

  public ShopAccount getDefaultAccount(DefaultAccount defaultAccount) {
    Shop shop = authProvider.getCurrentShop();
    return shopAccountRepository
        .findByAccountNameAndShop(defaultAccount.getAccountName(), shop)
        .orElseGet(
            () -> {
              ShopAccount shopAccount =
                  ShopAccount.builder()
                      .accountName(defaultAccount.getAccountName())
                      .accountType(defaultAccount.getAccountType().name())
                      .description(defaultAccount.getDescription())
                      .shop(shop)
                      .balance(0.0)
                      .isActive(true)
                      .build();
              return shopAccountRepository.save(shopAccount);
            });
  }
}
