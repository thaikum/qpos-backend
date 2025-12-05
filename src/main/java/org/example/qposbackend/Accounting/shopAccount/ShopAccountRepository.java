package org.example.qposbackend.Accounting.shopAccount;

import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopAccountRepository extends JpaRepository<ShopAccount, Long> {
    @Modifying
    @Query(nativeQuery = true, value = "update shop_account set balance = balance + :balance where id =:id")
    void updateAccountBalance(Long id, Double balance);

    Optional<ShopAccount> findByShopAndAccount_AccountName(Shop shop, String accountName);

    Optional<ShopAccount> findByShop_idAndAccount_id(Long shopId, Long accountId);

    @Modifying
    @Query("update ShopAccount set balance = :amount where shop =:shop and account =:account")
    void updateBalance(Shop shop, Account account, Double amount);

    List<ShopAccount> findAllByShop(Shop shop);

    boolean existsByShopAndAccount(Shop shop, Account account);

    Integer countAllByAccount(Account account);

    List<ShopAccount> findAllByAccountNameIsNull();
}