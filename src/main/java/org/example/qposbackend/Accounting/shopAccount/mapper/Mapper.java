package org.example.qposbackend.Accounting.shopAccount.mapper;

import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Accounting.shopAccount.dto.ShopAccountDto;

public class Mapper {
    public static ShopAccountDto shopAccountToShopAccountDto(ShopAccount shopAccount){
        ShopAccountDto dto = new ShopAccountDto();
        dto.setId(shopAccount.getId());
        dto.setAccountName(
                ObjectUtils.firstNonNull(
                        shopAccount.getDisplayName(), shopAccount.getAccount().getAccountName()));
        dto.setAccountNumber(shopAccount.getAccount().getAccountNumber());
        dto.setBalance(shopAccount.getBalance());
        dto.setCurrency(shopAccount.getCurrency());
        dto.setIsActive(shopAccount.getIsActive());
        dto.setAccountType(shopAccount.getAccount().getAccountType());
        dto.setDescription(
                ObjectUtils.firstNonNull(
                        shopAccount.getDisplayDescription(),
                        shopAccount.getAccount().getDescription()));
        return dto;
    }
}
