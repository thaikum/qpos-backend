package org.example.qposbackend.OffersAndPromotions.BundledConditions;

import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.Item.ItemClassification.Category.Category;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategory;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategory;

@Entity
@Data
public class BundledCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private InventoryItem item; // Relation to Item class

    @ManyToOne
    private Category category;

    @ManyToOne
    private SubCategory subCategory;

    @ManyToOne
    private MainCategory mainCategory;

    private Integer minQuantity;
    private Double minAmount;

}
