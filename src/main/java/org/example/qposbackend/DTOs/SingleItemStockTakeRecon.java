package org.example.qposbackend.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SingleItemStockTakeRecon {
    private Long stockTakeItemId;
    private Integer quantity;
}
