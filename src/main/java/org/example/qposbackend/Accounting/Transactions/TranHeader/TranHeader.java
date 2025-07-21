package org.example.qposbackend.Accounting.Transactions.TranHeader;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import lombok.*;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Integrity.IntegrityAttributes;

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
    private String status;
    private Date postedDate;
    private Date verifiedDate;
    @ManyToOne
    private UserShop postedBy;
    @ManyToOne
    private User verifiedBy;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="tran_header_id")
    private List<PartTran> partTrans;
}
