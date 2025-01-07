package org.example.qposbackend.OffersAndPromotions.BundledConditions;

import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.InventoryItem.InventoryItem;

@Entity
@Data
public class BundledCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private InventoryItem item; // Relation to Item class

    private Integer minQuantity;
    private String category;
    private Double minAmount;

}
