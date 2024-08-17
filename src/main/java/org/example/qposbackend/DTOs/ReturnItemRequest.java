package org.example.qposbackend.DTOs;

import org.example.qposbackend.Order.OrderItem.OrderItem;

public record ReturnItemRequest(Long orderItemId, String reason, Integer quantity, Double chargesIncurred) {
}
