package org.example.qposbackend.hirePurchase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.customer.Customer;
import org.example.qposbackend.hirePurchase.installments.Installment;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.shop.Shop;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HirePurchase extends IntegrityAttributes {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  @JoinColumn(nullable = false)
  @ManyToOne
  private Shop shop;

  @JoinColumn(nullable = false)
  @ManyToOne
  private Customer customer;

  private boolean itemReleased = false;

  @OneToMany
  private List<OrderItem> orderItems;

  // payment plan
  private LocalDate expectedCompletionDate;
  private LocalDate startDate;
  private Double interestApplied;
  private Double expectedTotalPay;
  private Double totalPaidAmount;

  @Transient
  @Getter(AccessLevel.NONE)
  private Double remainingAmount;

  @OneToMany(mappedBy = "hirePurchaseRef")
  private List<Installment> installments;

  // state
  @Enumerated(EnumType.STRING)
  private HirePurchaseStatus status;

  public Double getRemainingAmount() {
    return this.expectedTotalPay - this.totalPaidAmount;
  }
}
