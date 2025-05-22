package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.Exceptions.GenericRuntimeException;
import org.example.qposbackend.Utils.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("stock-take/recon-config")
@RequiredArgsConstructor
public class StockTakeReconTypeConfigController {
  private final StockTakeReconTypeConfigService stockTakeReconTypeConfigService;

  @PostMapping("create-stock-take-type-config")
  public ResponseEntity<MessageResponse> createStockTakeTypeConfig(
      @RequestBody @Valid StockTakeReconTypeConfig stockTakeReconTypeConfig) {
    try {
      stockTakeReconTypeConfigService.createStockTakeConfig(stockTakeReconTypeConfig);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new MessageResponse("stock-take-type-config created"));
    } catch (Exception e) {
      throw new GenericRuntimeException(e.getMessage());
    }
  }

  @PutMapping("update-stock-take-type-config/{id}")
  public ResponseEntity<MessageResponse> updateStockTakeTypeConfig(
      @RequestBody @Valid StockTakeReconTypeConfig stockTakeReconTypeConfig,
      @PathVariable Long id) {
    try {
      stockTakeReconTypeConfigService.updateStockTakeConfig(id, stockTakeReconTypeConfig);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new MessageResponse("stock-take-type-config updated"));
    } catch (Exception e) {
      throw new GenericRuntimeException(e.getMessage());
    }
  }

  @GetMapping("get-stock-take-type-config")
  public ResponseEntity<DataResponse> getStockTakeTypeConfig() {
    try {
      var configs = stockTakeReconTypeConfigService.getStockTakeReconTypeConfigs();
      return ResponseEntity.ok(new DataResponse(configs, null));
    } catch (Exception e) {
      throw new GenericRuntimeException(e.getMessage());
    }
  }

  @GetMapping("get-stock-take-recon-types")
  public ResponseEntity<DataResponse> getStockTakeReconTypes() {
    return ResponseEntity.ok(
        new DataResponse(EnumUtils.toEnumList(StockTakeReconType.class), null));
  }

  @GetMapping("get-stock-overage-cause")
  public ResponseEntity<DataResponse> getStockOverageCause() {
    return ResponseEntity.ok(new DataResponse(EnumUtils.toEnumList(StockOverageCause.class), null));
  }
}
