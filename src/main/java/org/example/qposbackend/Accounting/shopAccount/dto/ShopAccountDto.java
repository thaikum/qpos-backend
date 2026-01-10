package org.example.qposbackend.Accounting.shopAccount.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.qposbackend.Accounting.Accounts.Account;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShopAccountDto extends Account {
    private Boolean accountTypeIsEditable = false;
}
