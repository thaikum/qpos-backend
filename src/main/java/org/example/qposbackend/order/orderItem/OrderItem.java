package org.example.qposbackend.order.orderItem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.OffersAndPromotions.Offers.Offer;
import org.example.qposbackend.order.orderItem.ReturnInward.ReturnInward;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    private InventoryItem inventoryItem;
    private Double quantity;
    private double price;
    @JsonIgnore
    private double buyingPrice;
    private double discount;
    @Column(length = 20)
    private String discountMode;
    @OneToOne
    @JoinColumn
    private ReturnInward returnInward;
    @ManyToMany
    private List<Offer> offersApplied;
}
