package org.example.qposbackend.DTOs;

import lombok.Data;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;

import java.util.Date;

@Data
public class PriceDTO {
    private int id;
    private double buyingPrice;
    private double sellingPrice;
    private double discountAllowed;
    private PriceStatus status;
    private Date creationTimestamp;
}
