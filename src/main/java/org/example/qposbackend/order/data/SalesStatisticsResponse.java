package org.example.qposbackend.order.data;

import java.util.List;

public record SalesStatisticsResponse(
    List<SalesItemSummary> items,
    List<SalesItemSummary> returnedItems,
    double totalCash,
    double totalMpesa,
    double totalDebtors,
    double totalReturnedAmount
) {}
