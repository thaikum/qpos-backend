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

  @Transient
  @Getter(AccessLevel.NONE)
  private Double buyingPrice;

  @Transient
  @Getter(AccessLevel.NONE)
  private Double discountAllowed;

  public Double getSellingPrice() {
    Optional<Price> price =
        prices.stream().filter(p -> p.getStatus().equals(PriceStatus.ACTIVE)).findFirst();
    return price.orElse(this.prices.getLast()).getSellingPrice();
  }

  public Double getBuyingPrice() {
    Optional<Price> price =
        prices.stream().filter(p -> p.getStatus().equals(PriceStatus.ACTIVE)).findFirst();
    return price.orElse(this.prices.getLast()).getBuyingPrice();
  }

  public Double getDiscountAllowed() {
    Optional<Price> price =
        prices.stream().filter(p -> p.getStatus().equals(PriceStatus.ACTIVE)).findFirst();
    return price.orElse(this.prices.getLast()).getDiscountAllowed();
  }

  public Double getTotalBuyingPrice(int quantity) {
    if (quantity >= 0) {
      Price price = prices.getLast();
      return price.getBuyingPrice() * quantity;
    }

    quantity = Math.abs(quantity);
    double totalBuyingPrice = 0D;
    while (quantity > 0) {
      for (Price p : prices) {
        var existing = p.getQuantityUnderThisPrice();
        totalBuyingPrice += Math.min(existing, quantity) * p.getBuyingPrice();
        quantity = existing >= quantity ? 0 : quantity - existing;
      }
    }
    return totalBuyingPrice;
  }

  public void adjustInventoryQuantity(int quantityChange) {
    if (quantityChange >= 0) {
      Price price = prices.getLast();
      price.setQuantityUnderThisPrice(price.getQuantityUnderThisPrice() + quantityChange);
      return;
    }

    quantityChange = Math.abs(quantityChange);
    while (quantityChange > 0) {
      for (Price p : prices) {
        var existing = p.getQuantityUnderThisPrice();
        p.setQuantityUnderThisPrice(Math.max(0, existing - quantityChange));
        quantityChange = existing >= quantityChange ? 0 : quantityChange - existing;
      }
    }
  }
}
