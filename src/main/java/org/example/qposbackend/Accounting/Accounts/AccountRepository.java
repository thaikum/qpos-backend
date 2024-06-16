package org.example.qposbackend.Accounting.Accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountName(String accountName);

    Integer countByAccountType(String accountType);
    Optional<Account> findByAccountNumber(String accountNumber);
}
