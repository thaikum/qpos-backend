package org.example.qposbackend.order.lipaMdogoMdogo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.order.lipaMdogoMdogo.lipaMdogoMdogoOrder.LipaMdogoMdogoOrder;
import org.example.qposbackend.order.lipaMdogoMdogo.lipaMdogoMdogoPayment.LipaMdogoMdogoPayment;
import org.example.qposbackend.shop.Shop;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LipaMdogoMdogo extends IntegrityAttributes {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String customerName;
  private String customerPhone;
  private String customerIdNumber;
  @ManyToOne @JsonIgnore private Shop shop;
  private LocalDate dateOfFirstPayment;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "lipa_mdogo_mdogo_id")
  private List<LipaMdogoMdogoPayment> lipaMdogoMdogoPayments;

  @OneToOne(cascade = CascadeType.ALL)
  private LipaMdogoMdogoOrder relatedOrder;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private Double amountPaid;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private Double amountRemaining;

  public Double getAmountPaid() {
    amountPaid =
        lipaMdogoMdogoPayments.stream().mapToDouble(LipaMdogoMdogoPayment::getAmountPaid).sum();
    return amountPaid;
  }

  public Double getAmountRemaining() {
    double paid = getAmountPaid();
    double shouldBePaid =
        relatedOrder.getQuantity() * (relatedOrder.getPrice() - relatedOrder.getDiscountAllowed());
    amountRemaining = shouldBePaid - paid;
    return amountRemaining;
  }
}
