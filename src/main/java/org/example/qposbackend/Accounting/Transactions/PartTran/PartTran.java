package org.example.qposbackend.Accounting.Transactions.PartTran;

import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Integrity.IntegrityAttributes;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartTran {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer partTranNumber;
    private Character tranType;
    private Double amount;
    private String tranParticulars;
    @ManyToOne(cascade = {CascadeType.DETACH})
    @JoinColumn()
    private Account account;
}
