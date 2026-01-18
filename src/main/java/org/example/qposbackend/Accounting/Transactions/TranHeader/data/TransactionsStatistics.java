package org.example.qposbackend.Accounting.Transactions.TranHeader.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class TransactionsStatistics {
  int totalVerified;
  int totalDeclined;
  int totalUnverified;
  int totalPosted;

  @Getter(AccessLevel.NONE)
  int totalTransactions;

  public int getTotalTransactions() {
    return totalVerified + totalDeclined + totalUnverified + totalPosted;
  }
}
