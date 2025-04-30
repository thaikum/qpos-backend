package org.example.qposbackend.InventoryItem;

import jakarta.persistence.*;
import java.util.List;

import lombok.*;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.PriceDetails;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Suppliers.Supplier;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Item item;
    @Transient
    @Getter(AccessLevel.NONE)
    private Integer quantity;
    private Integer reorderLevel = 0;
    @Enumerated(EnumType.STRING)
    private InventoryStatus inventoryStatus;
    @ManyToMany(fetch = FetchType.LAZY)
    private List<Supplier> supplier;
    private Double buyingPrice;
    private Double sellingPrice;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private PriceDetails priceDetails;
    @Builder.Default
    private Double discountAllowed = 0.0;
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    public Integer getQuantity(){
    return this.priceDetails.getPrices().stream()
        .mapToInt(Price::getQuantityUnderThisPrice)
        .sum();
    }
}
