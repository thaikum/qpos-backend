package org.example.qposbackend.Accounting.Reports.Data;

public record ProfitPerCategoryRow(
    String category,
    Double totalQuantity,
    Double totalRevenue,
    Double totalCost,
    Double totalProfit,
    Double profitMargin
) {}
