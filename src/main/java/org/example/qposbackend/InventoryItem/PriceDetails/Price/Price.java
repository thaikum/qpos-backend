package org.example.qposbackend.InventoryItem.PriceDetails.Price;

import jakarta.persistence.*;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Price {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private double buyingPrice;
  private double sellingPrice;
  private double discountAllowed;
  private double quantityUnderThisPrice;

  @Enumerated(EnumType.STRING)
  private PriceStatus status;

  @Builder.Default private Date creationTimestamp = new Date();
  private Date stoppedOnTimestamp;
}
