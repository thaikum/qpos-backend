package org.example.qposbackend.Accounting.Accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountName(String accountName);

    Integer countByAccountType(String accountType);
    Optional<Account> findByAccountNumber(String accountNumber);

    @Query(nativeQuery = true, value = "update account set balance = balance + :balance where account_number = :accountNumber")
    void updateAccountBalance(String accountNumber, Double balance);
}
