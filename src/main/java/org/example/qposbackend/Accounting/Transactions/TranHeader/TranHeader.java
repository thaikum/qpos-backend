package org.example.qposbackend.Accounting.Transactions.TranHeader;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import lombok.*;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.shop.Shop;

@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TranHeader extends IntegrityAttributes{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long tranId;
    private Double totalAmount;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    private LocalDate postedDate;
    private LocalDate verifiedDate;
    @ManyToOne
    private UserShop postedBy;
    @ManyToOne
    private UserShop verifiedBy;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="tran_header_id")
    private List<PartTran> partTrans;
    private LocalDate rejectedDate;
    private String rejectionReason;
    @ManyToOne
    @JoinColumn(name = "rejected_by_id")
    private UserShop rejectedBy;
    @ManyToOne
    private Shop shop;
}
