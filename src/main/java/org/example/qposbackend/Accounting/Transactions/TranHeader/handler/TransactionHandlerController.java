package org.example.qposbackend.Accounting.Transactions.TranHeader.handler;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.TransactionsStatistics;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTran;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTranRequest;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("tran-handler")
public class TransactionHandlerController {
  private final TranHandlerService service;

  @PostMapping
  public ResponseEntity<DataResponse> createTransaction(@RequestBody HandlerTranRequest request) {
    var tran = service.createTransaction(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse(tran, null));
  }

  @GetMapping
  public ResponseEntity<DataResponse> getAllTransactionsByDate(
      @RequestParam LocalDate from,
      @RequestParam LocalDate to,
      @RequestParam(required = false, defaultValue = "VERIFIED") TransactionStatus status,
      @RequestParam Optional<TransactionCategory> category) {
    List<HandlerTran> transactions = service.getAllTransactionsByDate(from, to, category, status);
    log.info("Fetched transactions. Now sending to frontend.");
    return ResponseEntity.ok(
        new DataResponse(transactions, null));
  }

  @GetMapping("statistics")
  public ResponseEntity<DataResponse> getStatistics(
      @RequestParam LocalDate from,
      @RequestParam LocalDate to,
      @RequestParam Optional<TransactionCategory> category) {
    TransactionsStatistics stat = service.getStatistics(from, to, category);
    return ResponseEntity.ok(
        new DataResponse(stat, null));
  }
}
