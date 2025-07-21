package org.example.qposbackend.Accounting.Accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountRepository accountRepository;
  private final SpringSecurityAuditorAware auditorAware;

  @Bean
  private void initializeAccounts() {
    if (accountRepository.count() != 0) {
      return;
    }

    List<Account> accounts = new ArrayList<>();

    // sales account
    Account account =
        Account.builder()
            .accountName("SALES REVENUE")
            .accountNumber(String.format("%02d", AccountTypes.INCOME.ordinal()) + "001")
            .accountType(AccountTypes.INCOME.name())
            .description("Income from sales")
            .build();
    accounts.add(account);

    // stock
    account =
        Account.builder()
            .accountName("INVENTORY")
            .accountNumber(String.format("%02d", AccountTypes.ASSET.ordinal()) + "001")
            .accountType(AccountTypes.ASSET.name())
            .description("Goods for sale")
            .build();
    accounts.add(account);

    // cost of goods
    account =
        Account.builder()
            .accountName("COST OF GOODS")
            .accountNumber(String.format("%02d", AccountTypes.EXPENSE.ordinal()) + "001")
            .accountType(AccountTypes.EXPENSE.name())
            .description("")
            .build();
    accounts.add(account);

    // cash
    account =
        Account.builder()
            .accountName("CASH")
            .accountNumber(String.format("%02d", AccountTypes.ASSET.ordinal()) + "002")
            .accountType(AccountTypes.ASSET.name())
            .description("Cash at hand")
            .build();
    accounts.add(account);

    // MOBILE MONEY
    account =
        Account.builder()
            .accountName("MOBILE MONEY")
            .accountNumber(String.format("%02d", AccountTypes.ASSET.ordinal()) + "003")
            .accountType(AccountTypes.ASSET.name())
            .description("Amount in mobile money")
            .build();
    accounts.add(account);

    // RETAINED EARNINGS
    account =
        Account.builder()
            .accountName("RETAINED EARNINGS")
            .accountNumber(String.format("%02d", AccountTypes.INCOME.ordinal()) + "004")
            .accountType(AccountTypes.ASSET.name())
            .description("Accumulated profits")
            .build();
    accounts.add(account);

    accountRepository.saveAll(accounts);
  }

  public void createAccount(Account account) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    int accounts =
        accountRepository.countByShopAndAccountType(userShop.getShop(), account.getAccountType());
    String accountNumber =
        String.format(
            "%02d%03d", AccountTypes.valueOf(account.getAccountType()).ordinal(), accounts + 1);
    account.setAccountNumber(accountNumber);
    accountRepository.save(account);
  }

  public void updateAccount(Account account) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    Optional<Account> acOpt =
        accountRepository.findByShopAndAccountNumber(
            userShop.getShop(), account.getAccountNumber());

    if (acOpt.isPresent()) {
      Account ac = acOpt.get();
      ac.setAccountName(account.getAccountName());
      ac.setDescription(account.getDescription());
      ac.setBalance(account.getBalance());
      ac.setAccountType(account.getAccountType());
      accountRepository.save(ac);
    } else {
      throw new RuntimeException("No such account");
    }
  }

  public List<Account> findAllAccountsByShop() {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    return accountRepository.findAllByShop(userShop.getShop());
  }
}
