package org.example.qposbackend.DTOs;

import lombok.Builder;
import lombok.Data;
import org.example.qposbackend.InventoryItem.InventoryItem;

@Data
@Builder
public class StockTakeItemDTO {
    private Long id;
    private InventoryItem  inventoryItem;
    private Integer quantityDifference;
    private Double amountDifference;
}
