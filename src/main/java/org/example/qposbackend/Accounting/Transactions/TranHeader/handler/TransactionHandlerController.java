package org.example.qposbackend.Accounting.Transactions.TranHeader.handler;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.handlerTrans.HandlerTran;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("tran-handler")
public class TransactionHandlerController {
  private final TranHandlerService service;

  @PostMapping
  public ResponseEntity<DataResponse> createTransaction(@RequestBody HandlerTran handlerTran) {
    var tran = service.createTransaction(handlerTran);
    return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse(tran, null));
  }

  @GetMapping
  public ResponseEntity<DataResponse> getAllTransactionsByDate(
      @RequestParam LocalDate from,
      @RequestParam LocalDate to,
      @RequestParam(required = false, defaultValue = "VERIFIED") TransactionStatus status,
      @RequestParam Optional<TransactionCategory> category) {
    return ResponseEntity.ok(
        new DataResponse(service.getAllTransactionsByDate(from, to, category, status), null));
  }
}
