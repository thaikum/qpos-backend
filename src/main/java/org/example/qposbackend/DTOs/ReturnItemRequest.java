package org.example.qposbackend.DTOs;

public record ReturnItemRequest(Long orderItemId, String reason, Integer quantity, Double chargesIncurred) {
}
