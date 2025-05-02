package org.example.qposbackend.DTOs;

import lombok.Data;

@Data
public class SingleItemStockTakeRecon {
    private Long stockTakeItemId;
    private Integer quantity;
}
