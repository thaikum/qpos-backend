package org.example.qposbackend.Stock;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Stock.StockItem.StockItem;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date purchaseDate;

    @Temporal(TemporalType.DATE)
    private Date arrivalDate;

    @Column(nullable = false)
    private double transportCharges;

    @Column(nullable = false)
    private double otherCostsIncurred;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn
    private List<StockItem> items;
}
