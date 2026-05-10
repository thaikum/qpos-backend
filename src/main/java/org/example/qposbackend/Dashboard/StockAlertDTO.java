package org.example.qposbackend.Dashboard;

public record StockAlertDTO(
    String itemName,
    Double currentQuantity,
    Integer reorderLevel,
    String alertType
) {}
