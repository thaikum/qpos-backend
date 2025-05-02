package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.singleItemRecon;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItem;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleItemRecon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private StockTakeItem  stockTakeItem;
    private Integer quantity;
}
