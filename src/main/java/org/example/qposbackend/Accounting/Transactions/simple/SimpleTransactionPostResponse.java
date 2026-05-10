package org.example.qposbackend.Accounting.Transactions.simple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleTransactionPostResponse {
  private long simpleTransactionId;
  private long tranHeaderId;
  private String message;
}
