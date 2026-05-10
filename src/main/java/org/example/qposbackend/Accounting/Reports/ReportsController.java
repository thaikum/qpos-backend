package org.example.qposbackend.Accounting.Reports;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.example.qposbackend.Accounting.Reports.Data.DateWithAccount;
import org.example.qposbackend.Accounting.Reports.Data.DatesData;
import org.example.qposbackend.Accounting.Reports.Data.NumberOfDaysData;
import org.example.qposbackend.Accounting.Reports.Data.ProfitPerCategoryRow;
import org.example.qposbackend.Accounting.Reports.Data.ProfitPerItemRow;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("reports")
public class ReportsController {
    private final ReportsService reportsService;
    private final ProfitReportDataService profitReportDataService;

    public ReportsController(ReportsService reportsService, ProfitReportDataService profitReportDataService) {
        this.reportsService = reportsService;
        this.profitReportDataService = profitReportDataService;
    }

    @PostMapping("profit_and_loss")
    private ResponseEntity<Resource> generateProfitAndLoss(@RequestBody DatesData datesData) throws JRException, IOException, SQLException {
        byte[] reportContent = reportsService.generatePAndLReport(datesData);
        return resourceBuilder(reportContent);
    }

    @PostMapping("account_statement")
    private ResponseEntity<Resource> generateAccountStatement(@RequestBody DateWithAccount acData) throws JRException, IOException, SQLException {
        byte[] reportContent = reportsService.generateAccountStatement(acData);
        return resourceBuilder(reportContent);
    }

    @PostMapping("restocking_report")
    private ResponseEntity<Resource> generateRestockingReport(@RequestBody NumberOfDaysData numberOfDaysData) throws JRException, IOException, SQLException {
        byte[] reportContent = reportsService.generateRestockingReport(numberOfDaysData);
        return resourceBuilder(reportContent);
    }

    @PostMapping("profit_per_item")
    private ResponseEntity<Resource> generateProfitPerItem(@RequestBody DatesData datesData) throws JRException, IOException, SQLException {
        byte[] reportContent = reportsService.generateProfitPerItemReport(datesData);
        return resourceBuilder(reportContent);
    }

    @PostMapping("profit_per_category")
    private ResponseEntity<Resource> generateProfitPerCategory(@RequestBody DatesData datesData) throws JRException, IOException, SQLException {
        byte[] reportContent = reportsService.generateProfitPerCategoryReport(datesData);
        return resourceBuilder(reportContent);
    }

    @PostMapping("profit_per_item/data")
    public ResponseEntity<List<ProfitPerItemRow>> getProfitPerItemData(@RequestBody DatesData datesData) {
        return ResponseEntity.ok(profitReportDataService.getProfitPerItem(datesData));
    }

    @PostMapping("profit_per_category/data")
    public ResponseEntity<List<ProfitPerCategoryRow>> getProfitPerCategoryData(@RequestBody DatesData datesData) {
        return ResponseEntity.ok(profitReportDataService.getProfitPerCategory(datesData));
    }


    private ResponseEntity<Resource> resourceBuilder(byte [] byteStream){
        ByteArrayResource resource = new ByteArrayResource(byteStream);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("report")
                                .build().toString())
                .body(resource);
    }
}
