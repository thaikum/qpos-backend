package org.example.qposbackend.EOD;

public class EodBalanceMismatchException extends RuntimeException {
    private final double expectedTotal;
    private final double declaredTotal;
    private final double difference;

    public EodBalanceMismatchException(double expectedTotal, double declaredTotal) {
        super("EOD balance mismatch");
        this.expectedTotal = expectedTotal;
        this.declaredTotal = declaredTotal;
        this.difference = expectedTotal - declaredTotal;
    }

    public double getExpectedTotal() { return expectedTotal; }
    public double getDeclaredTotal() { return declaredTotal; }
    /** Positive = shortage (counted less than expected). Negative = overage. */
    public double getDifference() { return difference; }
}
