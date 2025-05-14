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
  private Integer expectedQuantity;
  private Integer actualQuantity;
  private Integer quantityDifference;
  private Double amountDifference;
}
