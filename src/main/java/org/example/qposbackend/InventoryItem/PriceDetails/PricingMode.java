package org.example.qposbackend.InventoryItem.PriceDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PricingMode {
    PERCENTAGE("Percentage", "Price to be calculated by give percentage"),
    FIXED_PROFIT("Fixed Profit", "Price to be calculated by adding the expected profit"),
    CUSTOM_SELLING_PRICE("Custom Selling Price", "Price set without profit in mind");

    private final String displayName;
    private final String description;
}
