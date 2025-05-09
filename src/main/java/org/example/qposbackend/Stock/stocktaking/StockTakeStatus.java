package org.example.qposbackend.Stock.stocktaking;

public enum StockTakeStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    UNRECONCILED,
    PARTIALLY_RECONCILED,
    EXPIRED,
    RECONCILED,
    CANCELLED,
    ON_HOLD
}
