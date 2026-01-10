package org.example.qposbackend.hirePurchase.installments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.hirePurchase.HirePurchase;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Installment extends IntegrityAttributes {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "hire_purchase_id", nullable = false)
  private HirePurchase hirePurchaseRef;

  private Double amountPaid;

  @Enumerated(EnumType.STRING)
  private ModeOfPayment modeOfPayment;

  @JoinColumn(nullable = false)
  @ManyToOne(cascade = CascadeType.MERGE)
  private TranHeader associatedTransaction;
}
