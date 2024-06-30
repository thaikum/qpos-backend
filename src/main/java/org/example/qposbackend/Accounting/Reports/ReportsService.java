package org.example.qposbackend.Accounting.Reports;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import org.example.qposbackend.Accounting.Reports.Data.DateWithAccount;
import org.example.qposbackend.Accounting.Reports.Data.DatesData;
import org.example.qposbackend.Accounting.Reports.Data.NumberOfDaysData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportsService {

    @Value("${files.resources}/reports")
    private String reportsPath;
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUsername;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    public byte[] generatePAndLReport(DatesData datesData) throws JRException, SQLException, FileNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", datesData.getStartDate());
        parameters.put("endDate", datesData.getEndDate());

        return generateJasperReport(parameters, "profit_and_loss_report.jrxml");
    }

    public byte[] generateAccountStatement(DateWithAccount acData) throws SQLException, JRException, FileNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", acData.getStartDate());
        parameters.put("endDate", acData.getEndDate());
        parameters.put("accountNumber", acData.getAccountNumber());

        return generateJasperReport(parameters, "account_statement.jrxml");
    }


    private byte[] generateJasperReport(Map<String, Object> parameters, String jasperFileName) throws SQLException, FileNotFoundException, JRException {
        String jdbcUrl = dbUrl.split("\\?")[0];
        Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

        JasperReport jasperReport;
        File file = ResourceUtils.getFile(reportsPath+"/"+jasperFileName);
        jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
        return baos.toByteArray();
    }

    public byte[] generateRestockingReport(NumberOfDaysData numberOfDaysData) throws JRException, SQLException, FileNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("days_to_stock", numberOfDaysData.getNumberOfDays());

        return generateJasperReport(parameters, "future_stocking_report.jrxml");
    }
}
