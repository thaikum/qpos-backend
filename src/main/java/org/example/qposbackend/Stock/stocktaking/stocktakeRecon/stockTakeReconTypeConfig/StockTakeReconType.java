package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockTakeReconType {
    UNRECORDED_SALE("Item/s was sold or used but the transaction was not recorded; the value exists as an overage in the system."),
    MISSING_ITEMS("Expected item/a is not found in stock, and no matching overage exists to offset the discrepancy."),
    DAMAGED_ITEMS("Item/s is present but in a faulty or unusable condition, requiring removal from sellable stock."),
    EXPIRED_ITEMS("Item/s is past its expiration date and cannot be sold or used."),
    EXCESS_ITEMS("More items are found in stock than expected, possibly due to unrecorded deliveries or errors."),
    INTERNAL_USE("Item was intentionally used for business operations (e.g., demo, repairs, or staff consumption).");
    private final String description;
}
