package org.example.qposbackend.EOD;

/**
 * Response body returned with HTTP 409 when EOD declared totals don't match the ledger.
 * difference > 0 means shortage (expected more than counted).
 * difference < 0 means overage (counted more than expected).
 */
public record EodBalanceMismatchBody(
        String message,
        double expectedTotal,
        double declaredTotal,
        double difference) {}
