package org.example.qposbackend.InventoryItem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.AuthorityFieldFilter.HideUnlessAuthorized;
import org.example.qposbackend.Item.Item;
import org.springframework.stereotype.Component;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Item item;
    private int quantity;
    private Double buyingPrice;
    private Double sellingPrice;
    @Builder.Default
    private Double discountAllowed = 0.0;
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
