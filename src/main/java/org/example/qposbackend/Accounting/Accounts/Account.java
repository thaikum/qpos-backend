package org.example.qposbackend.Accounting.Accounts;

import jakarta.persistence.*;
import lombok.*;
import org.example.qposbackend.Integrity.IntegrityAttributes;

@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account extends IntegrityAttributes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 50)
    private String accountNumber;
    @Column(unique = true, nullable = false, length = 50)
    private String accountName;
    @Column(nullable = false, length = 20)
    private String accountType;
    @Column(nullable = false, length = 100)
    private String description;
    @Builder.Default
    private Double balance = 0.0;
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "KES";
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
