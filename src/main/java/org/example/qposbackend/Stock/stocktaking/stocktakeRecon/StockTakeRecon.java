package org.example.qposbackend.Stock.stocktaking.stocktakeRecon;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.singleItemRecon.SingleItemRecon;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconType;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTakeRecon {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Double reconciliationAmount;

  private Double penaltyAmount;

  @Temporal(TemporalType.DATE)
  private Date reconDate;

  private String description;

  @ManyToOne private Account penaltyAccount;

  @ManyToOne private Account deductionAccount;

  @Enumerated(EnumType.STRING)
  private StockTakeReconType stockTakeReconType;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn
  private List<SingleItemRecon> singleItemRecons;
}
