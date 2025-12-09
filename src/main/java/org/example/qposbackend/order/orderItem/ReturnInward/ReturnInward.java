package org.example.qposbackend.order.orderItem.ReturnInward;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReturnInward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Builder.Default
    private int quantityReturned = 0;
    private String returnReason;
    private Date dateSold;
    private Date dateReturned;
    @Builder.Default
    private Double costIncurred = 0D;
}
