package org.example.qposbackend.Stock.stocktaking.data;

import lombok.Data;

import java.util.List;

@Data
public class StockTakeItemReconDto{
    private Long stockTakeItemId;
    private Double quantity; // In case the approver finds it different
    private List<DiscrepancyCategorizationDto>  discrepancyCategoryList;
}
