package org.example.qposbackend.Accounting.Transactions.simple;

import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("simple-transactions")
@RequiredArgsConstructor
public class SimpleTransactionController {

  private final SimpleTransactionService simpleTransactionService;

  @PostMapping
  public ResponseEntity<DataResponse> record(
      @RequestBody @Valid SimpleTransactionPostRequest request) {
    try {
      SimpleTransactionPostResponse resp = simpleTransactionService.record(request);
      return ResponseEntity.ok(new DataResponse(resp, null));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new DataResponse(null, e.getMessage()));
    } catch (Exception e) {
      log.error("Simple transaction failed", e);
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
          .body(new DataResponse(null, e.getMessage()));
    }
  }

  @GetMapping
  public ResponseEntity<DataResponse> list(
      @RequestParam LocalDate from, @RequestParam LocalDate to) {
    try {
      return ResponseEntity.ok(
          new DataResponse(simpleTransactionService.listBetween(from, to), null));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new DataResponse(null, e.getMessage()));
    }
  }
}
