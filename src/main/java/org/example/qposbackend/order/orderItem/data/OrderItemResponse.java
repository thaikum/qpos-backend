package org.example.qposbackend.order.orderItem.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private String itemName;
    private Double quantity;
    private double price;
    private double buyingPrice;
    private double discount;
}
