package org.example.qposbackend.Stock.stocktaking;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.StockTakeRecon;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItem;
import org.jfree.util.ObjectUtilities;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockTake extends IntegrityAttributes {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Temporal(TemporalType.DATE)
  private Date stockTakeDate;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "stock_take_id")
  private List<StockTakeItem> stockTakeItems;

  @Transient
  @Enumerated(EnumType.STRING)
  @Getter(AccessLevel.NONE)
  private StockTakeStatus status;

  @Enumerated(EnumType.STRING)
  private StockTakeType stockTakeType;

  @Transient private String stockTakeValue;

  @ManyToOne(cascade = {CascadeType.MERGE})
  private User assignedUser;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn
  private List<StockTakeRecon> stockTakeRecons;

  public StockTakeStatus getStatus() {
    boolean someDone = false;
    boolean someNotDone = false;
    boolean someNotReconciled = false;
    boolean containsDiscrepancies = false;

    Map<Long, Integer> stockTakeReconciliation = new HashMap<>();

    ObjectUtils.firstNonNull(stockTakeRecons, new ArrayList<StockTakeRecon>())
        .forEach(
            stockTakeRecon -> {
              stockTakeRecon
                  .getSingleItemRecons()
                  .forEach(
                      stockTakeRec -> {
                        stockTakeReconciliation.compute(
                            stockTakeRec.getStockTakeItem().getId(),
                            (key, value) -> {
                              StockTakeItem item = stockTakeRec.getStockTakeItem();
                              if (value == null) {
                                return (item.getQuantity() + stockTakeRec.getQuantity());
                              } else {
                                return value + stockTakeRec.getQuantity();
                              }
                            });
                      });
            });

    for (StockTakeItem stockTakeItem : stockTakeItems) {
      if (stockTakeItem.getQuantity() == null) {
        someNotDone = true;
      } else {
        someDone = true;
        if (!stockTakeItem.getQuantity().equals(stockTakeItem.getExpected())) {
          containsDiscrepancies = true;
        }
      }

      if (!Objects.equals(
          stockTakeItem.getExpected(),
          stockTakeReconciliation.getOrDefault(stockTakeItem.getId(), -33400))) {
        someNotReconciled = true;
      }
    }

    if (someDone && someNotDone) {
      return StockTakeStatus.IN_PROGRESS;
    } else if (someNotDone) {
      return StockTakeStatus.SCHEDULED;
    } else if (someDone && stockTakeReconciliation.isEmpty()) {
      if (containsDiscrepancies) {
        return StockTakeStatus.UNRECONCILED;
      } else {
        return StockTakeStatus.COMPLETED;
      }
    } else if (!stockTakeReconciliation.isEmpty() && someNotReconciled) {
      return StockTakeStatus.PARTIALLY_RECONCILED;
    } else {
      return StockTakeStatus.RECONCILED;
    }
  }
}
