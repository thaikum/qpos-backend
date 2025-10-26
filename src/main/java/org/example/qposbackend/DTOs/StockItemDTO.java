package org.example.qposbackend.DTOs;

public record StockItemDTO(Long item, Double buyingPrice,  Integer packaging,
                           Double quantity) {
}
