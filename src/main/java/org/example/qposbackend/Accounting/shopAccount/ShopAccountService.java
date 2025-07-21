package org.example.qposbackend.Accounting.shopAccount;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.shop.Shop;
import org.example.qposbackend.shop.ShopRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopAccountService {
    private final ShopAccountRepository shopAccountRepository;
    private final AccountRepository accountRepository;
    private final SpringSecurityAuditorAware auditorAware;
    private final ShopRepository shopRepository;

    @Transactional
    public ShopAccount createShopAccount(Long accountId) {
        UserShop userShop = auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new NoSuchElementException("User not found"));

    Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new NoSuchElementException("Account not found"));

        if (shopAccountRepository.existsByShopAndAccount(userShop.getShop(), account)) {
            throw new IllegalStateException("Shop account already exists");
        }

        ShopAccount shopAccount = ShopAccount.builder()
                .shop(userShop.getShop())
                .account(account)
                .balance(0.0)
                .currency(account.getCurrency())
                .isActive(true)
                .build();

        return shopAccountRepository.save(shopAccount);
    }

    public List<ShopAccount> getShopAccounts() {
        UserShop userShop = auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return shopAccountRepository.findAllByShop(userShop.getShop());
    }

    @Transactional
    public void updateBalance(Shop shop, Account account, Double amount) {
        shopAccountRepository.updateBalance(shop, account, amount);
    }

    public ShopAccount getShopAccount(Long id) {
        UserShop userShop = auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new NoSuchElementException("User not found"));

    return shopAccountRepository
        .findById(id)
        .filter(sa -> sa.getShop().equals(userShop.getShop()))
        .orElseThrow(() -> new NoSuchElementException("Shop account not found"));
    }

    @Transactional
    public void toggleAccountStatus(Long id) {
        ShopAccount shopAccount = getShopAccount(id);
        shopAccount.setIsActive(!shopAccount.getIsActive());
        shopAccountRepository.save(shopAccount);
    }

    @Bean
    private boolean migrateAllAccounts(ShopRepository shopRepository, AccountRepository accountRepository, ShopAccountRepository shopAccountRepository){
        List<Shop> shops = shopRepository.findAll();
        List<Account> accounts = accountRepository.findAll();
        List<ShopAccount> shopAccounts = new ArrayList<>();
        for(Shop shop : shops){
            for(Account account : accounts){
                if(shopAccountRepository.existsByShopAndAccount(shop, account)){
                    continue;
                }

                ShopAccount shopAccount = ShopAccount.builder()
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