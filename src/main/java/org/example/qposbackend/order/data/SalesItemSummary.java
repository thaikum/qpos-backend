package org.example.qposbackend.order.data;

public record SalesItemSummary(
    Long inventoryItemId,
    String itemName,
    double quantity,
    double price,
    double discount,
    double total
) {}
