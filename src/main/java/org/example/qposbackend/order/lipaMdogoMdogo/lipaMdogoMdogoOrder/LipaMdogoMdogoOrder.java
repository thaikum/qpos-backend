package org.example.qposbackend.order.lipaMdogoMdogo.lipaMdogoMdogoOrder;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.InventoryItem.InventoryItem;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LipaMdogoMdogoOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private InventoryItem inventoryItem;
    private int quantity;
    private double price;
    private double discountAllowed;
}
