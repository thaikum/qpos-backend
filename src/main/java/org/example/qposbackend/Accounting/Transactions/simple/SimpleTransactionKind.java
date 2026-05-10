package org.example.qposbackend.Accounting.Transactions.simple;

public enum SimpleTransactionKind {
  /** Money spent on supplies, transport, repairs, etc. */
  EXPENSE,
  /** Money the owner took from the till or mobile for personal use. */
  OWNERS_WITHDRAWAL,
  /** Till or drawer had more cash than expected. */
  CASH_OVERAGE,
  /** Till or drawer had less cash than expected. */
  CASH_SHORTAGE,
}
