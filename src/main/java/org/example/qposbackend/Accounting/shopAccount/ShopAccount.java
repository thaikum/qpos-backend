package org.example.qposbackend.Accounting.shopAccount;

import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.shop.Shop;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShopAccount extends IntegrityAttributes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Deprecated
    @ManyToOne(cascade = CascadeType.MERGE)
    private Account account;
    private String accountName;
    private String accountNumber;
    @Column(nullable = false, length = 20)
    private String accountType;
    private String description;
    @ManyToOne(cascade = CascadeType.MERGE)
    private Shop shop;
    private Double balance;
    private String currency;
    private Boolean isActive;
    @Builder.Default
    private Boolean isEditable = true;
}
