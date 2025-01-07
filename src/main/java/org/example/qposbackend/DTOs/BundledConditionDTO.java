package org.example.qposbackend.DTOs;

import lombok.Data;

@Data
public class BundledConditionDTO {
    private Long itemId;
    private Integer minQuantity;
    private String category;
    private Double minAmount;
}
