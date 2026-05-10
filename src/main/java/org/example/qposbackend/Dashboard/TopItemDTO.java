package org.example.qposbackend.Dashboard;

public record TopItemDTO(
    String itemName,
    Double totalRevenue,
    Double totalProfit,
    Double totalQuantity
) {}
