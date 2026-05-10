package org.example.qposbackend.Dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * JDBC-backed dashboard aggregates — keeps native SQL out of services.
 */
@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String REVENUE_SUMMARY_SQL = """
            SELECT
                COALESCE(SUM(CASE WHEN DATE(so.date) = CURDATE()
                    THEN oi.price * oi.quantity ELSE 0 END), 0)                       AS revenue_today,
                COALESCE(SUM(CASE WHEN YEARWEEK(so.date, 1) = YEARWEEK(CURDATE(), 1)
                    THEN oi.price * oi.quantity ELSE 0 END), 0)                       AS revenue_week,
                COALESCE(SUM(CASE WHEN YEAR(so.date) = YEAR(CURDATE()) AND MONTH(so.date) = MONTH(CURDATE())
                    THEN oi.price * oi.quantity ELSE 0 END), 0)                       AS revenue_month,
                COALESCE(SUM(CASE WHEN DATE(so.date) = CURDATE()
                    THEN (oi.price - oi.buying_price) * oi.quantity ELSE 0 END), 0)   AS profit_today,
                COALESCE(SUM(CASE WHEN YEARWEEK(so.date, 1) = YEARWEEK(CURDATE(), 1)
                    THEN (oi.price - oi.buying_price) * oi.quantity ELSE 0 END), 0)   AS profit_week,
                COALESCE(SUM(CASE WHEN YEAR(so.date) = YEAR(CURDATE()) AND MONTH(so.date) = MONTH(CURDATE())
                    THEN (oi.price - oi.buying_price) * oi.quantity ELSE 0 END), 0)   AS profit_month,
                COUNT(DISTINCT CASE WHEN DATE(so.date) = CURDATE() THEN so.id END)    AS sales_today,
                COUNT(DISTINCT CASE WHEN YEARWEEK(so.date, 1) = YEARWEEK(CURDATE(), 1) THEN so.id END) AS sales_week,
                COUNT(DISTINCT CASE WHEN YEAR(so.date) = YEAR(CURDATE()) AND MONTH(so.date) = MONTH(CURDATE())
                    THEN so.id END)                                                    AS sales_month
            FROM sale_order so
            JOIN order_item oi ON so.id = oi.order_items_id
            WHERE so.shop_id = ? AND oi.return_inward_id IS NULL
            """;

    private static final String STOCK_COUNTS_SQL = """
            SELECT
                COUNT(CASE WHEN COALESCE(sq.total_qty, 0) = 0 THEN 1 END)          AS out_of_stock,
                COUNT(CASE WHEN COALESCE(sq.total_qty, 0) > 0
                           AND ii.reorder_level >= 0
                           AND COALESCE(sq.total_qty, 0) <= ii.reorder_level THEN 1 END) AS below_reorder
            FROM inventory_item ii
            LEFT JOIN (
                SELECT p.prices_id, SUM(p.quantity_under_this_price) AS total_qty
                FROM price p
                GROUP BY p.prices_id
            ) sq ON sq.prices_id = ii.price_details_id
            WHERE ii.shop_id = ? AND ii.is_deleted = 0 AND ii.reorder_level >= 0
            """;

    /**
     * %s is replaced only by {@link TopItemsSortColumn#columnAlias()} — never external input.
     */
    private static final String TOP_ITEMS_TEMPLATE = """
            SELECT
                i.name                                                            AS item_name,
                ROUND(SUM(oi.price * oi.quantity), 2)                            AS total_revenue,
                ROUND(SUM((oi.price - oi.buying_price) * oi.quantity), 2)        AS total_profit,
                ROUND(SUM(oi.quantity), 4)                                        AS total_quantity
            FROM order_item oi
            JOIN inventory_item ii ON oi.inventory_item_id = ii.id
            JOIN item i ON ii.item_id = i.id
            JOIN sale_order so ON so.id = oi.order_items_id
            WHERE so.shop_id = ?
              AND YEAR(so.date) = ?
              AND MONTH(so.date) = ?
              AND oi.return_inward_id IS NULL
            GROUP BY i.id, i.name
            ORDER BY %s DESC
            LIMIT 5
            """;

    private static final String STOCK_ALERTS_SQL = """
            SELECT
                i.name                                              AS item_name,
                COALESCE(SUM(p.quantity_under_this_price), 0)       AS current_quantity,
                ii.reorder_level,
                CASE
                    WHEN COALESCE(SUM(p.quantity_under_this_price), 0) = 0 THEN 'OUT_OF_STOCK'
                    ELSE 'LOW_STOCK'
                END                                                 AS alert_type
            FROM inventory_item ii
            JOIN item i ON ii.item_id = i.id
            LEFT JOIN price p ON p.prices_id = ii.price_details_id
            WHERE ii.shop_id = ? AND ii.is_deleted = 0 AND ii.reorder_level >= 0
            GROUP BY ii.id, i.name, ii.reorder_level
            HAVING COALESCE(SUM(p.quantity_under_this_price), 0) <= ii.reorder_level
            ORDER BY current_quantity ASC
            """;

    private static final String SLOW_MOVERS_SQL = """
            SELECT
                i.name                                                                        AS item_name,
                COALESCE(i.category, 'Uncategorized')                                         AS category,
                ROUND(SUM(CASE WHEN DATE(so.date) < DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                               THEN oi.price * oi.quantity ELSE 0 END) / 3, 2)               AS avg_monthly_revenue,
                ROUND(COALESCE(SUM(CASE WHEN DATE(so.date) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                                       THEN oi.price * oi.quantity ELSE 0 END), 0), 2)       AS recent_revenue,
                DATE_FORMAT(MAX(so.date), '%Y-%m-%d')                                         AS last_sale_date
            FROM order_item oi
            JOIN inventory_item ii ON oi.inventory_item_id = ii.id
            JOIN item i ON ii.item_id = i.id
            JOIN sale_order so ON so.id = oi.order_items_id
            WHERE so.shop_id = ?
              AND DATE(so.date) >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH)
              AND oi.return_inward_id IS NULL
            GROUP BY i.id, i.name, i.category
            HAVING avg_monthly_revenue > 0 AND recent_revenue = 0
            ORDER BY avg_monthly_revenue DESC
            LIMIT 20
            """;

    public Map<String, Object> findRevenueSummary(long shopId) {
        return jdbcTemplate.queryForMap(REVENUE_SUMMARY_SQL, shopId);
    }

    public Map<String, Object> findStockCounts(long shopId) {
        return jdbcTemplate.queryForMap(STOCK_COUNTS_SQL, shopId);
    }

    public List<TopItemDTO> findTopItemsForMonth(long shopId, int year, int month, TopItemsSortColumn sortBy) {
        String sql = TOP_ITEMS_TEMPLATE.formatted(sortBy.columnAlias());
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new TopItemDTO(
                        rs.getString("item_name"),
                        rs.getDouble("total_revenue"),
                        rs.getDouble("total_profit"),
                        rs.getDouble("total_quantity")
                ),
                shopId, year, month);
    }

    public List<StockAlertDTO> findStockAlerts(long shopId) {
        return jdbcTemplate.query(STOCK_ALERTS_SQL,
                (rs, rowNum) -> new StockAlertDTO(
                        rs.getString("item_name"),
                        rs.getDouble("current_quantity"),
                        rs.getInt("reorder_level"),
                        rs.getString("alert_type")
                ),
                shopId);
    }

    public List<SlowMoverDTO> findSlowMovers(long shopId) {
        return jdbcTemplate.query(SLOW_MOVERS_SQL,
                (rs, rowNum) -> new SlowMoverDTO(
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getDouble("avg_monthly_revenue"),
                        rs.getDouble("recent_revenue"),
                        rs.getString("last_sale_date")
                ),
                shopId);
    }

    /** Allowed ORDER BY columns for top-items query — maps API tokens safely to SQL identifiers. */
    public enum TopItemsSortColumn {
        REVENUE("total_revenue"),
        PROFIT("total_profit"),
        QUANTITY("total_quantity");

        private final String columnAlias;

        TopItemsSortColumn(String columnAlias) {
            this.columnAlias = columnAlias;
        }

        public String columnAlias() {
            return columnAlias;
        }

        public static TopItemsSortColumn fromApiToken(String token) {
            if (token == null || token.isBlank()) {
                return REVENUE;
            }
            return switch (token.trim().toLowerCase()) {
                case "profit" -> PROFIT;
                case "quantity" -> QUANTITY;
                default -> REVENUE;
            };
        }
    }
}
