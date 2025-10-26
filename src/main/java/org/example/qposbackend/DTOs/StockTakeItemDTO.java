package org.example.qposbackend.DTOs;

import lombok.Builder;
import lombok.Data;
import org.example.qposbackend.InventoryItem.InventoryItem;

@Data
@Builder
public class StockTakeItemDTO {
  private Long id;
  private String itemName;
  private Double itemPrice;
  private Double expectedQuantity;
  private Double actualQuantity;
  private Double quantityDifference;
  private Double amountDifference;
  private Double alreadyReconciled;
}
