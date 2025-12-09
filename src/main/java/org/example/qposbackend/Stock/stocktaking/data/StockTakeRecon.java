package org.example.qposbackend.Stock.stocktaking.data;

import lombok.Data;

import java.util.List;

@Data
public class StockTakeRecon {
    private long stockTakeId;
    List<StockTakeItemReconDto> stockTakeItems;
}
