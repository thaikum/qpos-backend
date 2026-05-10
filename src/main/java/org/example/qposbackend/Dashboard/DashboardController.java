package org.example.qposbackend.Dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("top-items")
    public ResponseEntity<List<TopItemDTO>> getTopItems(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "revenue") String sortBy) {
        return ResponseEntity.ok(dashboardService.getTopItemsForMonth(year, month, sortBy));
    }

    @GetMapping("stock-alerts")
    public ResponseEntity<List<StockAlertDTO>> getStockAlerts() {
        return ResponseEntity.ok(dashboardService.getStockAlerts());
    }

    @GetMapping("slow-movers")
    public ResponseEntity<List<SlowMoverDTO>> getSlowMovers() {
        return ResponseEntity.ok(dashboardService.getSlowMovers());
    }
}
