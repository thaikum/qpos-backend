package org.example.qposbackend.Stock.stocktaking.stocktakeItem;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;

@Slf4j
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockTakeItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "inventory_item_id")
  private InventoryItem inventoryItem;

  private Double quantity = null;

  @Setter(AccessLevel.NONE)
  private Double expected; // this value should be set on object creation

  @Getter(AccessLevel.NONE)
  @Transient
  private Double amountDifference;

  public Double getAmountDifference() {
    var amount =
        (ObjectUtils.firstNonNull(this.quantity, 0D) -  this.getExpected())
            * this.inventoryItem.getPriceDetails().getSellingPrice();
    log.info("amountDifference: {}", amount);
    return amount;
  }

  @PrePersist
  private void setExpectedOnCreation() {
    this.expected =
        inventoryItem.getPriceDetails().getPrices().stream()
            .mapToDouble(Price::getQuantityUnderThisPrice)
            .sum();
  }
}
