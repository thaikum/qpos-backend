package org.example.qposbackend.Stock.stocktaking.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
