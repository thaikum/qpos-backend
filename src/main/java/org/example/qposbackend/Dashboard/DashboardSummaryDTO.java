package org.example.qposbackend.Dashboard;

public record DashboardSummaryDTO(
    Double totalRevenueToday,
    Double totalRevenueThisWeek,
    Double totalRevenueThisMonth,
    Double totalProfitToday,
    Double totalProfitThisWeek,
    Double totalProfitThisMonth,
    Long totalSalesToday,
    Long totalSalesThisWeek,
    Long totalSalesThisMonth,
    Long outOfStockCount,
    Long belowReorderLevelCount
) {}
