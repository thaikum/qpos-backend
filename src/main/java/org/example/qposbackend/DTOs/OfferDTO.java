package org.example.qposbackend.DTOs;

import lombok.Data;
import org.example.qposbackend.OffersAndPromotions.DiscountType;
import org.example.qposbackend.OffersAndPromotions.OfferType;

import java.util.Date;
import java.util.List;

@Data
public class OfferDTO {
    private Long id;
    private String offerName;
    private String description;
    private OfferType offerType;
    private DiscountType discountType;
    private Double discountAllowed;
    private String category;
    private List<Long> itemsIds; // List of item IDs
    private List<BundledConditionDTO> bundledConditions; // Bundled conditions
    private boolean isActive;
    private Date creationDate;
    private Date endDate;
}
