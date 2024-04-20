package org.example.qposbackend.Item;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 50)
    private String barCode;
    @Column(length = 50)
    private String name;
    private String imageUrl;
    @Column(length = 50)
    private String mainCategory;
    @Column(length = 50)
    private String category;
    private String subCategory;
    private Double buyingPrice;
    private Double minSellingPrice;
    private Double maxSellingPrice;
    @Column(length = 50)
    private String brand;
    @Column(length = 50)
    private String color;
}
