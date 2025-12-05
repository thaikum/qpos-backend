package org.example.qposbackend.Stock.stocktaking.data;

public record StockItemDTO(Long item, Double buyingPrice,  Integer packaging,
                           Double quantity) {
}
