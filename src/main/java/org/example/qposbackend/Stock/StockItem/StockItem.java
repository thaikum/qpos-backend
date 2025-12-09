package org.example.qposbackend.Stock.StockItem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.InventoryItem.InventoryItem;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private InventoryItem item;

    @Column(nullable = false)
    private double buyingPrice;

    @Builder.Default
    private Integer packaging = 1;

    @Column(nullable = false)
    private Double quantity;
}
