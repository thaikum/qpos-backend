package org.example.qposbackend.Accounting.Accounts;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Bean
    private void initializeAccounts() {
        if (accountRepository.count() != 0) {
            return;
        }

        List<Account> accounts = new ArrayList<>();

        //sales account
        Account account = Account.builder()
                .accountName("SALES REVENUE")
                .accountNumber(String.format("%02d", AccountTypes.INCOME.ordinal()) + "001")
                .accountType(AccountTypes.INCOME.name())
                .description("Income from sales")
                .build();
        accounts.add(account);

        // stock
        account = Account.builder()
                .accountName("INVENTORY")
                .accountNumber(String.format("%02d", AccountTypes.ASSET.ordinal()) + "001")
                .accountType(AccountTypes.ASSET.name())
                .description("Goods for sale")
                .build();
        accounts.add(account);

        //cost of goods
        account = Account.builder()
                .accountName("COST OF GOODS")
                .accountNumber(String.format("%02d", AccountTypes.EXPENSE.ordinal()) + "001")
                .accountType(AccountTypes.EXPENSE.name())
                .description("")
                .build();
        accounts.add(account);

        //cash
        account = Account.builder()
                .accountName("CASH")
                .accountNumber(String.format("%02d", AccountTypes.ASSET.ordinal()) + "002")
                .accountType(AccountTypes.ASSET.name())
                .description("Cash at hand")
                .build();
        accounts.add(account);

        //MOBILE MONEY
        account = Account.builder()
                .accountName("MOBILE MONEY")
                .accountNumber(String.format("%02d", AccountTypes.ASSET.ordinal()) + "003")
                .accountType(AccountTypes.ASSET.name())
                .description("Amount in mobile money")
                .build();
        accounts.add(account);

        //RETAINED EARNINGS
        account = Account.builder()
                .accountName("RETAINED EARNINGS")
                .accountNumber(String.format("%02d", AccountTypes.INCOME.ordinal()) + "004")
                .accountType(AccountTypes.ASSET.name())
                .description("Accumulated profits")
                .build();
        accounts.add(account);

        accountRepository.saveAll(accounts);
    }
}
