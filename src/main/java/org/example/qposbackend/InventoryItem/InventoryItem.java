package org.example.qposbackend.InventoryItem;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;
import org.example.qposbackend.InventoryItem.PriceDetails.PriceDetails;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Suppliers.Supplier;
import org.example.qposbackend.shop.Shop;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne private Item item;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private Integer quantity;

  @Builder.Default
  private Integer reorderLevel = 0;

  @Enumerated(EnumType.STRING)
  private InventoryStatus inventoryStatus;

  @ManyToMany(fetch = FetchType.LAZY)
  private List<Supplier> supplier;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private Double buyingPrice;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private Double sellingPrice;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private PriceDetails priceDetails;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @Builder.Default
  private Double discountAllowed = 0.0;

  @Builder.Default
  @Column(nullable = false)
  private boolean isDeleted = false;

  @ManyToOne
  @JoinColumn(name = "shop_id")
  private Shop shop;

  public Double getQuantity() {
    return ObjectUtils.firstNonNull(this.priceDetails.getPrices(), new ArrayList<Price>()).stream()
        .mapToDouble(Price::getQuantityUnderThisPrice)
        .sum();
  }

  public Price getActivePrice() {
    return this.priceDetails.getPrices().stream()
        .filter(p -> p.getStatus() == PriceStatus.ACTIVE)
        .findFirst()
        .orElse(null);
  }

  public Double getDiscountAllowed() {
    return getActivePrice().getDiscountAllowed();
  }

  public Double getSellingPrice() {
    return getActivePrice().getSellingPrice();
  }

  public Double getBuyingPrice() {
    return getActivePrice().getBuyingPrice();
  }
}
