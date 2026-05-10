package org.example.qposbackend.Dashboard;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SpringSecurityAuditorAware auditorAware;
    private final DashboardRepository dashboardRepository;

    private Long getShopId() {
        UserShop userShop = auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found"));
        return userShop.getShop().getId();
    }

    public DashboardSummaryDTO getSummary() {
        long shopId = getShopId();
        var revenue = dashboardRepository.findRevenueSummary(shopId);
        var stock = dashboardRepository.findStockCounts(shopId);

        return new DashboardSummaryDTO(
                toDouble(revenue.get("revenue_today")),
                toDouble(revenue.get("revenue_week")),
                toDouble(revenue.get("revenue_month")),
                toDouble(revenue.get("profit_today")),
                toDouble(revenue.get("profit_week")),
                toDouble(revenue.get("profit_month")),
                toLong(revenue.get("sales_today")),
                toLong(revenue.get("sales_week")),
                toLong(revenue.get("sales_month")),
                toLong(stock.get("out_of_stock")),
                toLong(stock.get("below_reorder"))
        );
    }

    public List<TopItemDTO> getTopItemsForMonth(int year, int month, String sortBy) {
        long shopId = getShopId();
        DashboardRepository.TopItemsSortColumn column =
                DashboardRepository.TopItemsSortColumn.fromApiToken(sortBy);
        return dashboardRepository.findTopItemsForMonth(shopId, year, month, column);
    }

    public List<StockAlertDTO> getStockAlerts() {
        return dashboardRepository.findStockAlerts(getShopId());
    }

    /** Items that averaged good revenue 1–4 months ago but earned nothing in the last 30 days. */
    public List<SlowMoverDTO> getSlowMovers() {
        return dashboardRepository.findSlowMovers(getShopId());
    }

    private Double toDouble(Object value) {
        return value instanceof Number n ? n.doubleValue() : 0.0;
    }

    private Long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }
}
