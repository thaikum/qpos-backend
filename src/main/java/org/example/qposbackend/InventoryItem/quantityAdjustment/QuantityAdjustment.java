package org.example.qposbackend.InventoryItem.quantityAdjustment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.InventoryItem.InventoryItem;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuantityAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double initialQuantity;
    private Double adjustmentQuantity;
    private String adjustmentReason;
    @ManyToOne
    private UserShop adjustedBy;
    private LocalDateTime adjustedOn;
    @ManyToOne
    private InventoryItem item;
}
