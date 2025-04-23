package org.example.qposbackend.InventoryItem.PriceDetails.Price;

import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.Integrity.IntegrityAttributes;

import java.util.Date;

@Entity
@Data
public class Price{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private double buyingPrice;
    private double sellingPrice;
    private double discountAllowed;
    private int quantityUnderThisPrice;
    @Enumerated(EnumType.STRING)
    private PriceStatus status;
    private Date creationTimestamp = new Date();
    private Date stoppedOnTimestamp;
}
