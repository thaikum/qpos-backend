package org.example.qposbackend.InventoryItem.PriceDetails;

import graphql.util.Pair;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.*;
import org.example.qposbackend.Exceptions.GenericRuntimeException;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private PricingMode pricingMode;

  private Double profitPercentage;
  private Double fixedProfit;

  @JoinColumn
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
    Price active = resolveActivePrice();
    return active == null ? 0D : active.getSellingPrice();
  }

  public Double getBuyingPrice() {
    Price active = resolveActivePrice();
    return active == null ? 0D : active.getBuyingPrice();
  }

  public Double getDiscountAllowed() {
    Price active = resolveActivePrice();
    return active == null ? 0D : active.getDiscountAllowed();
  }

  private Price resolveActivePrice() {
    if (prices == null || prices.isEmpty()) return null;
    return prices.stream()
        .filter(p -> p.getStatus() == PriceStatus.ACTIVE)
        .findFirst()
        .orElseGet(() -> prices.get(prices.size() - 1));
  }

  public Double getTotalBuyingPrice(Double quantity) {
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

  /** Adjusts inventory quantity based on price tiers */
  public void adjustInventoryQuantity(Double quantityChange) {
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

  public List<Pair<Double, Price>> getBuyingPriceBrokenDownPerTheQuantity(Double quantity) {
    List<Pair<Double, Price>> result = new ArrayList<>();
    List<Price> sortedPrices =
        prices.stream().sorted(Comparator.comparingInt(Price::getId)).toList();
    for (Price price : sortedPrices) {
      double quantityDeducted = Math.min(quantity, price.getQuantityUnderThisPrice());
      quantity -= quantityDeducted;
      if (quantityDeducted > 0) {
        result.add(new Pair<>(quantityDeducted, price));
      }
      if (quantity == 0) return result;
    }
    throw new GenericRuntimeException("Not enough stock");
  }
}
