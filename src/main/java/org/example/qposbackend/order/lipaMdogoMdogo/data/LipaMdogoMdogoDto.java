package org.example.qposbackend.order.lipaMdogoMdogo.data;

import lombok.Data;
import org.example.qposbackend.InventoryItem.InventoryItem;

@Data
public class LipaMdogoMdogoDto {
    private Double amountPaid;
    private Double customerName;
    private Double customerPhone;
    private Double customerIdNumber;
    private Long inventoryItemId;
    private Double discountAllowed;
    private Double quantity;
}
