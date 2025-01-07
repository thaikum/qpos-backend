package org.example.qposbackend.EOD;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.User.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EOD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Builder.Default
    private LocalDate date = LocalDate.now();
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    @ManyToOne
    private User user;
    @Builder.Default
    private Double balanceBroughtDownCash = 0D;
    @Builder.Default
    private Double balanceBroughtDownMobile = 0D;
    @Builder.Default
    private Double balanceBroughtDownBank  = 0D;
    @Builder.Default
    private Double totalRecoveredDebt  = 0D;
    @Builder.Default
    private Double totalDebtors  = 0D;
    @Builder.Default
    private Double totalCreditors = 0D;
    @Builder.Default
    private Double totalCashSale  = 0D;
    @Builder.Default
    private Double totalMobileSale  = 0D;
}
