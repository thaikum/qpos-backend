package org.example.qposbackend.Accounting.Reports;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.example.qposbackend.Accounting.Reports.Data.DateWithAccount;
import org.example.qposbackend.Accounting.Reports.Data.DatesData;
import org.example.qposbackend.Accounting.Reports.Data.NumberOfDaysData;
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

@RestController
@Slf4j
@RequestMapping("reports")
public class ReportsController {
    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
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
