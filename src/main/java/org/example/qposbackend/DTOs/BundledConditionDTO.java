package org.example.qposbackend.DTOs;

import lombok.Data;

@Data
public class BundledConditionDTO {
    private Long valueId;
    private Integer minQuantity;
    private Double minAmount;
}
