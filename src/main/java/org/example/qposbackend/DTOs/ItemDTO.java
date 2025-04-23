package org.example.qposbackend.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemDTO {
    @NotNull
    private String barCode;

    @NotNull
    private String unitOfMeasure;

    @NotNull
    private Integer minimumPerUnit;

    @NotNull
    private String name;

    private String imageUrl;

    @NotNull
    private String subCategory;

    private String brand;

    private String description;
}
