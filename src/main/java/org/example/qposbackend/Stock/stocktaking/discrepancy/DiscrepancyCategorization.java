package org.example.qposbackend.Stock.stocktaking.discrepancy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfig;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscrepancyCategorization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private StockTakeReconTypeConfig reconTypeConfig;
    private double quantity;
    private boolean deductEmployee = false;
    @ManyToOne
    private UserShop employeeToDeduct;
    private boolean isReconciled;

}
