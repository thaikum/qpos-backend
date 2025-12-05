package org.example.qposbackend.Accounting.Transactions.PartTran;

import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.ShopAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PartTranService {
  @Bean
  public boolean migratePartTrans(
      PartTranRepository partTranRepository, ShopAccountRepository shopAccountRepository) {
    List<PartTran> partTranList = partTranRepository.findAllByShopAccountIsNullAndAccountNotNull();
    for (PartTran partTran : partTranList) {
      Optional<ShopAccount> shopAccount =
          shopAccountRepository.findByShop_idAndAccount_id(1L, partTran.getAccount().getId());
      shopAccount.ifPresent(partTran::setShopAccount);
    }
    partTranRepository.saveAll(partTranList);
    return true;
  }
}
