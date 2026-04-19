package org.example.qposbackend.Stock.stocktaking.stocktakeItem;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.Stock.stocktaking.StockTakeService;
import org.example.qposbackend.Stock.stocktaking.data.StockTakeItemReconDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("stock-take/stock-take-item")
@RequiredArgsConstructor
public class StockTakeItemController {
  private final StockTakeItemService stockTakeItemService;
  private final StockTakeService stockTakeService;

  @PutMapping
  public ResponseEntity<MessageResponse> updateStockTakeItem(@RequestBody StockTakeItemDto stockTakeItem) {
    try {
      stockTakeItemService.updateStockTakeItem(stockTakeItem);
      stockTakeService.refreshStatusAfterItemCount(stockTakeItem.id());
      return ResponseEntity.ok(new MessageResponse("stock-take-item-updated"));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new MessageResponse(ex.getMessage()));
    }
  }

    @PostMapping("save-recon-updates")
    public ResponseEntity<MessageResponse> saveReconItem(StockTakeItemReconDto reconDto) {
        stockTakeItemService.saveStockTakeItem(reconDto);
        return ResponseEntity.ok(new MessageResponse("Reconciliation successful"));
    }
}
