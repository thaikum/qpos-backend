package org.example.qposbackend.Stock.stocktaking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.Stock.stocktaking.stocktakeItem.StockTakeItem;
import org.example.qposbackend.shop.Shop;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockTake extends IntegrityAttributes {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Temporal(TemporalType.DATE)
  private Date stockTakeDate;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "stock_take_id")
  private List<StockTakeItem> stockTakeItems;

  @Enumerated(EnumType.STRING)
  @Getter(AccessLevel.NONE)
  private StockTakeStatus status;

  @Enumerated(EnumType.STRING)
  private StockTakeType stockTakeType;

  @Transient private String stockTakeValue;

  @ManyToOne(cascade = {CascadeType.MERGE})
  private User assignedUser;

  @ManyToOne @JoinColumn @JsonIgnore private Shop shop;
}
