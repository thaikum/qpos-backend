package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.stockTakeReconTypeConfigValidator.ValidStockOverageCause;

@Entity
@Data
@ValidStockOverageCause
public class StockTakeReconTypeConfig {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private StockTakeReconType stockTakeReconType;

  @Enumerated(EnumType.STRING)
  private StockOverageCause stockOverageCause =
      null; // should only be set if stockTakeReconType is excess;

  private Boolean createSale = false;
  private Boolean applyPenalty = false;
  private Boolean hasFinancialImpact = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private ShopAccount penaltyAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private ShopAccount expenseAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private ShopAccount balancingAccount;

  private Boolean isDeleted = false;
}
