package org.example.qposbackend.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryItemDTO {
    private ItemDTO item;

    private PriceDetailsDTO priceDetails;

    @NotNull
    private Integer stockQuantity;

    private Integer reorderLevel;

    private String inventoryStatus;

    private String supplier;
}
