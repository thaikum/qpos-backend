package org.example.qposbackend.order.orderItem.data;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long inventoryItemId;
    private double quantity;
    private double discount;
}
