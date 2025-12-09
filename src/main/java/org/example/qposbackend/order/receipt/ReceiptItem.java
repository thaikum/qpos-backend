package org.example.qposbackend.order.receipt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceiptItem {
    private String itemName;
    private double quantity;
    private double unitPrice;
    private double total;
}
