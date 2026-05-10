package org.example.qposbackend.Dashboard;

public record SlowMoverDTO(
    String itemName,
    String category,
    Double avgMonthlyRevenue,
    Double recentRevenue,
    String lastSaleDate
) {}
