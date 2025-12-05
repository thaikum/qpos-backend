package org.example.qposbackend.DTOs;

import lombok.Builder;
import lombok.Data;
import org.example.qposbackend.Stock.stocktaking.data.SingleItemStockTakeRecon;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconType;

import java.util.List;

@Data
@Builder
public class GroupItemsStockTakeRecon {
    private long id;
    private List<SingleItemStockTakeRecon> singleItemStockTakeRecons;
    private String description;
    private Long targetAccountId;
    private StockTakeReconType stockTakeReconType;
    private Double penalty;
    private Long penaltyAccountId;
}
