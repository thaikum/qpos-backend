package org.example.qposbackend.Accounting.Reports.Data;

public record ProfitPerItemRow(
    String itemName,
    String category,
    Double totalQuantity,
    Double totalRevenue,
    Double totalCost,
    Double totalProfit,
    Double profitMargin
) {}
