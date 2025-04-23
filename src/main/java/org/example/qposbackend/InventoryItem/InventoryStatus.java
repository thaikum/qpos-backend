package org.example.qposbackend.InventoryItem;

import lombok.*;

@RequiredArgsConstructor
@Getter
public enum InventoryStatus {
    AVAILABLE("Available", "Item is available for sale"),
    OUT_OF_STOCK("Out of stock", "Item is out of stock"),
    DISCONTINUED("Discontinued", "Item is no longer in sale"),
    PRE_ORDER("Pre-order", "Item is new and marked for ordering"),
    INACTIVE("Inactive", "Item is temporarily out of sale");

    private final String displayName;
    private final String description;
}
