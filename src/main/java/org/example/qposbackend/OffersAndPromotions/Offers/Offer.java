package org.example.qposbackend.OffersAndPromotions.Offers;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.ItemClassification.Category.Category;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategory;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategory;
import org.example.qposbackend.OffersAndPromotions.BundledConditions.BundledCondition;
import org.example.qposbackend.OffersAndPromotions.DiscountType;
import org.example.qposbackend.OffersAndPromotions.OfferBasedOn;
import org.example.qposbackend.OffersAndPromotions.OfferOn;
import org.example.qposbackend.OffersAndPromotions.OfferType;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class Offer extends IntegrityAttributes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String offerName;

    private String description;

    @Enumerated(EnumType.STRING)
    private OfferOn effectOn;

    @Enumerated(EnumType.STRING)
    private OfferBasedOn basedOn;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Double discountAllowed;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Category> categories;

    @ManyToMany
    private List<MainCategory> mainCategories;

    @ManyToMany
    private List<SubCategory> subCategories;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryItem> items;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "offer_id")
    private List<BundledCondition> bundledConditions;

    private Double minAmount;

    @JsonProperty("isActive")
    private boolean isActive;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    private Integer minQuantity;
    private Boolean applyMultipleOnSameOrder = false;
    private Double maxDiscountPerOrder = null;
}
