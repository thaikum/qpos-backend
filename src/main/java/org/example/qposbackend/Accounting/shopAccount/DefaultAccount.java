package org.example.qposbackend.Accounting.shopAccount;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Accounts.AccountTypes;
import org.example.qposbackend.shop.Shop;

@Getter
@RequiredArgsConstructor
public enum DefaultAccount {
  CASH("CASH", AccountTypes.ASSET, "Represents physical currency and coins available for immediate use."),
  SALES_REVENUE("SALES REVENUE", AccountTypes.INCOME, "Tracks total earnings from the sale of goods and services."),
  MOBILE_MONEY("MOBILE MONEY", AccountTypes.ASSET, "Represents funds held in digital wallets and mobile payment platforms."),
  CUSTOMER_ADVANCES("CUSTOMER ADVANCES", AccountTypes.LIABILITY, "Tracks prepayments received from customers for orders yet to be fulfilled."),
  INVENTORY("INVENTORY", AccountTypes.ASSET, "Represents the total value of goods held in stock for resale."),
  COST_OF_GOODS("COST OF GOODS", AccountTypes.EXPENSE, "Records the direct costs associated with purchasing or producing sold items."),
  ACCOUNTS_RECEIVABLE("ACCOUNTS RECEIVABLE", AccountTypes.ASSET, "Tracks outstanding amounts owed by customers for credit sales.");

  private final String accountName;
  private final AccountTypes accountType;
  private final String description;
}
