package org.example.qposbackend.InventoryItem.PriceDetails;

import jakarta.persistence.*;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;

@Entity
@Data
public class PriceDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private PricingMode pricingMode;

  private Double profitPercentage;
  private Double fixedProfit;

  @JoinColumn
  @OneToMany(cascade = CascadeType.ALL)
  private List<Price> prices;

  @Transient
  @Getter(AccessLevel.NONE)
  private Double sellingPrice;

  public Double getSellingPrice() {
    Optional<Price> price =
        prices.stream().filter(p -> p.getStatus().equals(PriceStatus.ACTIVE)).findFirst();
    return price.orElse(this.prices.getLast()).getSellingPrice();
  }
}
