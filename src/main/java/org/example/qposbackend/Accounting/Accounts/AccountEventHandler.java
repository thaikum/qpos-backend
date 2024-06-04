package org.example.qposbackend.Accounting.Accounts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
@Slf4j
public class AccountEventHandler {
    @Autowired
    private AccountRepository accountRepository;

    @HandleBeforeCreate
    public void handleAccountBeforeCreate(Account account) {
        int accounts = accountRepository.countByAccountType(account.getAccountType());
        String accountNumber = String.format("%02d%03d", AccountTypes.valueOf(account.getAccountType()).ordinal(), accounts + 1);
        log.info("Account Number: {}, {}", accountNumber, account);
        account.setAccountNumber(accountNumber);
    }
}
