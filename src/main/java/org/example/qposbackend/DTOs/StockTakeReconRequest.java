package org.example.qposbackend.DTOs;

import java.util.List;
import lombok.Data;
import org.example.qposbackend.Accounting.Accounts.Account;

@Data
public class StockTakeReconRequest {
    private Long stockTakeId;
    private List<GroupItemsStockTakeRecon> groupItemsStockTakeRecons;
}
