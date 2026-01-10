package org.example.qposbackend.hirePurchase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.hirePurchase.data.HirePurchaseRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("hire-purchase")
public class HirePurchaseController {
  private final HirePurchaseService hirePurchaseService;

  @PostMapping
  public ResponseEntity<DataResponse> createHirePurchase(
      @Valid @RequestBody HirePurchaseRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new DataResponse(
                hirePurchaseService.createHirePurchaseAndPrintReceipt(request), "Lipa mdogo mdogo started"));
  }

  @GetMapping
  public ResponseEntity<DataResponse> getAllHirePurchase(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {
    return ResponseEntity.ok(new DataResponse(hirePurchaseService.getAllByDate(from, to), null));
  }

  @GetMapping("/{id}")
  public ResponseEntity<DataResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(new DataResponse(hirePurchaseService.getById(id), null));
  }
}
