package org.example.qposbackend.Accounting.Accounts;

import org.example.qposbackend.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountName(String accountName);

    Integer countByShopAndAccountType(Shop shop, String accountType);
    Optional<Account> findByShopAndAccountNumber(Shop shop, String accountNumber);

    List<Account> findAllByShop(Shop shop);
}
