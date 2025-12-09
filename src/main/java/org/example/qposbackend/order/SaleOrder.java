package org.example.qposbackend.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.shop.Shop;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleOrder extends IntegrityAttributes {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn
  private List<OrderItem> orderItems;

  private double discount;
  @Builder.Default private Date date = new Date();
  private String modeOfPayment;
  @Builder.Default private Double amountInCash = 0D;
  @Builder.Default private Double amountInMpesa = 0D;
  @Builder.Default private Double amountInCredit = 0D;

  @ManyToOne
  @JoinColumn(name = "shop_id")
  @JsonIgnore
  private Shop shop;
}
