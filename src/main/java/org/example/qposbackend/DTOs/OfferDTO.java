package org.example.qposbackend.DTOs;

import lombok.Data;
import org.example.qposbackend.OffersAndPromotions.DiscountType;
import org.example.qposbackend.OffersAndPromotions.OfferBasedOn;
import org.example.qposbackend.OffersAndPromotions.OfferOn;
import org.example.qposbackend.OffersAndPromotions.OfferType;

import java.util.Date;
import java.util.List;

@Data
public class OfferDTO {
    private String offerName;
    private String description;
    private OfferType offerType;
    private DiscountType discountType;
    private Double discountAllowed;
    private String category;
    private List<BundledConditionDTO> bundledConditions;
    private boolean isActive;
    private OfferOn effectOn;
    private String subCategory;
    private Integer quantity;
    private Double minAmount;
    private OfferBasedOn basedOn;
    private List<Long> affectedIds;
    private Date startDate;
    private Date endDate;
    private Boolean applyMultipleOnSameOrder;
    private Double maxDiscountPerOrder;
}

