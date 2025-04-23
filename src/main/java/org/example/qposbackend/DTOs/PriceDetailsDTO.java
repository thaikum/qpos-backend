package org.example.qposbackend.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PriceDetailsDTO {
    @NotNull
    private String pricingMode;

    private Integer profitPercentage;

    private Integer fixedProfit;

    private List<PriceDTO> prices;

}
