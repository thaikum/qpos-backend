package org.example.qposbackend.InventoryItem.PriceDetails;

import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;

import java.util.List;

@Entity
@Data
public class PriceDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private PricingMode pricingMode;
    private Double profitPercentage;
    private Double fixedProfit;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn
    private List<Price> prices;
}
