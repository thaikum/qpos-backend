package org.example.qposbackend.Accounting.Transactions.PartTran;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Accounting.shopAccount.ShopAccount;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartTran{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer partTranNumber;
    private Character tranType;
    private Double amount;
    private String tranParticulars;

    @Deprecated
    @ManyToOne(cascade = {CascadeType.DETACH})
    @JoinColumn()
    private Account account;

    @ManyToOne
    @JoinColumn()
    private ShopAccount shopAccount;
}
