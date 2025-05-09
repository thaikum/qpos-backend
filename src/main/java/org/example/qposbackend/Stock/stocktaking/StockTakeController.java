package org.example.qposbackend.Stock.stocktaking;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.*;
import org.example.qposbackend.Exceptions.GenericRuntimeException;
import org.example.qposbackend.Utils.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("stock-take")
public class StockTakeController {
  private final StockTakeService stockTakeService;

  @PostMapping("create")
  public ResponseEntity<DataResponse> createStockTake(
      @RequestBody StockTakeRequest stockTakeRequest) {
    try {
      StockTake stockTake =
          stockTakeService.createStockTake(
              stockTakeRequest.stockTakeType(), stockTakeRequest.ids(), stockTakeRequest.date());
      return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse(stockTake, null));
    } catch (Exception e) {
      throw new GenericRuntimeException(e.getMessage());
    }
  }

  @PostMapping("schedule/for-user")
  public ResponseEntity<DataResponse> createStockTake(
      @RequestBody StockTakeWithUserRequest stockTakeRequest) {
    try {
      StockTake stockTake =
          stockTakeService.createStockTake(
              stockTakeRequest.stockTakeType(),
              stockTakeRequest.ids(),
              stockTakeRequest.date(),
              stockTakeRequest.user());
      return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse(stockTake, null));
    } catch (Exception e) {
      throw new GenericRuntimeException(e.getMessage());
    }
  }

  @PutMapping
  public ResponseEntity<MessageResponse> performStockTake(@RequestBody StockTake stockTake) {
    try {
      stockTakeService.performStockTake(stockTake);
      return ResponseEntity.ok().body(new MessageResponse("OK"));
    } catch (Exception e) {
      throw new GenericRuntimeException(e.getMessage());
    }
  }

  @GetMapping("discrepancies/{id}")
  public ResponseEntity<DataResponse> createStockTake(@PathVariable long id) {
    try {
      StockTakeDTO stockTakeDTO = stockTakeService.getDiscrepancies(id);
      return ResponseEntity.ok(new DataResponse(stockTakeDTO, null));
    } catch (Exception e) {
      throw new GenericRuntimeException(e.getMessage());
    }
  }

  @GetMapping("get-stock-take-type")
  public ResponseEntity<DataResponse> getStockTakeType() {
    return ResponseEntity.ok(new DataResponse(EnumUtils.toEnumList(StockTakeType.class), null));
  }

  @GetMapping
  public ResponseEntity<DataResponse> getStockTake() {
    return ResponseEntity.ok(new DataResponse(stockTakeService.getStockTakes(), null));
  }

  @GetMapping("{id}")
  public ResponseEntity<DataResponse> getStockTake(@PathVariable long id) {
    return ResponseEntity.ok(new DataResponse(stockTakeService.getStockTake(id), null));
  }

  @PostMapping("reconcile")
  public ResponseEntity<DataResponse> reconcileStockTake(
      StockTakeReconRequest stockTakeReconRequest) {
    try {
      StockTakeDTO stockTakeDTO = stockTakeService.reconcileStockTake(stockTakeReconRequest);
      return ResponseEntity.ok(new DataResponse(stockTakeDTO, null));
    } catch (Exception e) {
      throw new GenericRuntimeException(e.getMessage());
    }
  }
}
