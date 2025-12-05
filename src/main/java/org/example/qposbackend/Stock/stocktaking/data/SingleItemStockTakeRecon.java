package org.example.qposbackend.Stock.stocktaking.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SingleItemStockTakeRecon {
    private Long stockTakeItemId;
    private Double quantity;
}
