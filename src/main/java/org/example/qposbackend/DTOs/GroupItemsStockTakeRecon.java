package org.example.qposbackend.DTOs;

import lombok.Data;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconType;

import java.util.List;

@Data
public class GroupItemsStockTakeRecon {
    private List<SingleItemStockTakeRecon> singleItemStockTakeRecons;
    private String description;
    private Long targetAccountId;
    private StockTakeReconType stockTakeReconType;
    private Double penalty;
    private Long penaltyAccountId;
}
