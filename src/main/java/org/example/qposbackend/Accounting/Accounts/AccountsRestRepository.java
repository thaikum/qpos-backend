package org.example.qposbackend.Accounting.Accounts;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.web.bind.annotation.RequestMapping;


public interface AccountsRestRepository extends PagingAndSortingRepository<Account, Long> {
}
