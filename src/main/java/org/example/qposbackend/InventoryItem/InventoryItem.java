package org.example.qposbackend.InventoryItem;

import com.fasterxml.jackson.annotation.JsonProperty;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

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
  @Setter(AccessLevel.NONE)
  private Integer quantity;

  @Builder.Default
  private Integer reorderLevel = 0;

  @Enumerated(EnumType.STRING)
  private InventoryStatus inventoryStatus;

  @ManyToMany(fetch = FetchType.LAZY)
  private List<Supplier> supplier;

  @Transient
  @Setter(AccessLevel.NONE)
  private Double buyingPrice;

  @Transient
  @Setter(AccessLevel.NONE)
  private Double sellingPrice;

  @Transient
  @Setter(AccessLevel.NONE)
  @Builder.Default
  private Double discountAllowed = 0.0;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private PriceDetails priceDetails;

  @Builder.Default
  @Column(nullable = false)
  private boolean isDeleted = false;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "shop_id")
  private Shop shop;

  @JsonProperty("quantity")
  public Double getQuantity() {
    return ObjectUtils.firstNonNull(this.priceDetails.getPrices(), new ArrayList<Price>()).stream()
        .mapToDouble(Price::getQuantityUnderThisPrice)
        .sum();
  }

  @JsonProperty("discountAllowed")
  public Double getDiscountAllowed() {
    return this.getPriceDetails().getDiscountAllowed();
  }

  @JsonProperty("sellingPrice")
  public Double getSellingPrice() {
    return getPriceDetails().getSellingPrice();
  }

  @JsonProperty("buyingPrice")
  public Double getBuyingPrice() {
    return getPriceDetails().getBuyingPrice();
  }
}