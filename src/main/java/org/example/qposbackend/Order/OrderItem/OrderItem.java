package org.example.qposbackend.Order.OrderItem;

import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.Order.OrderItem.ReturnInward.ReturnInward;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    private InventoryItem inventoryItem;
    private int quantity;
    private double price;
    private double discount;
    @Column(length = 20)
    private String discountMode;
    @OneToOne
    @JoinColumn
    private ReturnInward returnInward;
}
