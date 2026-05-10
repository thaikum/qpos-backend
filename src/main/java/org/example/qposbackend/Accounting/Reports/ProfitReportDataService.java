package org.example.qposbackend.Accounting.Reports;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Reports.Data.DatesData;
import org.example.qposbackend.Accounting.Reports.Data.ProfitPerCategoryRow;
import org.example.qposbackend.Accounting.Reports.Data.ProfitPerItemRow;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProfitReportDataService {

    private final JdbcTemplate jdbcTemplate;
    private final SpringSecurityAuditorAware auditorAware;

    private Long getShopId() {
        UserShop userShop = auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found"));
        return userShop.getShop().getId();
    }

    public List<ProfitPerItemRow> getProfitPerItem(DatesData dates) {
        Long shopId = getShopId();
        String sql = """
                SELECT
                    i.name                                                                         AS item_name,
                    COALESCE(i.category, 'Uncategorized')                                          AS category,
                    ROUND(SUM(oi.quantity), 4)                                                     AS total_quantity,
                    ROUND(SUM(oi.price * oi.quantity), 2)                                          AS total_revenue,
                    ROUND(SUM(oi.buying_price * oi.quantity), 2)                                   AS total_cost,
                    ROUND(SUM((oi.price - oi.buying_price) * oi.quantity), 2)                     AS total_profit,
                    ROUND(100.0 * SUM((oi.price - oi.buying_price) * oi.quantity)
                          / NULLIF(SUM(oi.price * oi.quantity), 0), 2)                            AS profit_margin
                FROM order_item oi
                JOIN inventory_item ii ON oi.inventory_item_id = ii.id
                JOIN item i ON ii.item_id = i.id
                JOIN sale_order so ON so.id = oi.order_items_id
                WHERE so.shop_id = ?
                  AND DATE(so.date) BETWEEN DATE(?) AND DATE(?)
                  AND oi.return_inward_id IS NULL
                GROUP BY i.id, i.name, i.category
                ORDER BY total_profit DESC
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new ProfitPerItemRow(
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getDouble("total_quantity"),
                        rs.getDouble("total_revenue"),
                        rs.getDouble("total_cost"),
                        rs.getDouble("total_profit"),
                        rs.getDouble("profit_margin")
                ),
                shopId, dates.getStartDate(), dates.getEndDate());
    }

    public List<ProfitPerCategoryRow> getProfitPerCategory(DatesData dates) {
        Long shopId = getShopId();
        String sql = """
                SELECT
                    COALESCE(i.category, 'Uncategorized')                                         AS category,
                    ROUND(SUM(oi.quantity), 4)                                                     AS total_quantity,
                    ROUND(SUM(oi.price * oi.quantity), 2)                                          AS total_revenue,
                    ROUND(SUM(oi.buying_price * oi.quantity), 2)                                   AS total_cost,
                    ROUND(SUM((oi.price - oi.buying_price) * oi.quantity), 2)                     AS total_profit,
                    ROUND(100.0 * SUM((oi.price - oi.buying_price) * oi.quantity)
                          / NULLIF(SUM(oi.price * oi.quantity), 0), 2)                            AS profit_margin
                FROM order_item oi
                JOIN inventory_item ii ON oi.inventory_item_id = ii.id
                JOIN item i ON ii.item_id = i.id
                JOIN sale_order so ON so.id = oi.order_items_id
                WHERE so.shop_id = ?
                  AND DATE(so.date) BETWEEN DATE(?) AND DATE(?)
                  AND oi.return_inward_id IS NULL
                GROUP BY i.category
                ORDER BY total_profit DESC
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new ProfitPerCategoryRow(
                        rs.getString("category"),
                        rs.getDouble("total_quantity"),
                        rs.getDouble("total_revenue"),
                        rs.getDouble("total_cost"),
                        rs.getDouble("total_profit"),
                        rs.getDouble("profit_margin")
                ),
                shopId, dates.getStartDate(), dates.getEndDate());
    }
}
