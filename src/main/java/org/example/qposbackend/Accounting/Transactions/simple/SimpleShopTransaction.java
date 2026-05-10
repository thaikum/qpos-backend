package org.example.qposbackend.Accounting.Transactions.simple;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.shop.Shop;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleShopTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Shop shop;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "posted_by_id")
  private UserShop postedBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private SimpleTransactionKind kind;

  /** Where cash was paid from or adjusted (cash vs mobile wallet). */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private SimplePaymentSource paymentSource;

  @Column(nullable = false)
  private Double amount;

  @Column(nullable = false, length = 2000)
  private String particular;

  @Column(nullable = false)
  private LocalDate postedDate;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private TranHeader tranHeader;
}
