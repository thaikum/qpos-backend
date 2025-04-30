package org.example.qposbackend.Stock.stocktaking.stocktakeItem;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;

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

  private Integer quantity;

  @Setter(AccessLevel.NONE)
  private Integer expected; // this value should be set on object creation

  @Getter(AccessLevel.NONE)
  @Transient
  private Double amountDifference;

  public Double getAmountDifference() {
    return this.getExpected()
        - this.getQuantity() * this.inventoryItem.getPriceDetails().getSellingPrice();
  }

  @PrePersist
  private void setExpectedOnCreation() {
    this.quantity =
        inventoryItem.getPriceDetails().getPrices().stream()
            .mapToInt(Price::getQuantityUnderThisPrice)
            .sum();
  }
}
