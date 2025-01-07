package org.example.qposbackend.OffersAndPromotions.Offers;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.OffersAndPromotions.BundledConditions.BundledCondition;
import org.example.qposbackend.OffersAndPromotions.DiscountType;
import org.example.qposbackend.OffersAndPromotions.OfferType;

import java.util.Date;
import java.util.List;

@Entity
@Data
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String offerName;
    private String description;

    @Enumerated(EnumType.STRING)
    private OfferType offerType;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Double discountAllowed;

    private String category;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryItem> items;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "offer_id")
    private List<BundledCondition> bundledConditions;

    private Double minAmount;

    @JsonProperty("isActive")
    private boolean isActive;
    private Date creationDate;
    private Date endDate;
}
