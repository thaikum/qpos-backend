package org.example.qposbackend.Accounting.Transactions.simple;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleTransactionListItem {
  private long id;
  private SimpleTransactionKind kind;
  private SimplePaymentSource paymentSource;
  private double amount;
  private String particular;
  private String postedDate;
  private long tranHeaderId;
  private String tranStatus;
}
