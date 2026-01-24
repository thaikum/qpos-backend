package org.example.qposbackend.Accounting.Transactions.TranHeader.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TransactionCategory;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranHeaderResponseDTO {
    private Long tranId;
    private Double totalAmount;
    private TransactionStatus status;
    private LocalDate postedDate;
    private LocalDate verifiedDate;
    private LocalDate rejectedDate;
    private TransactionCategory tranCategory;
    private String description;
    
    // User information for postedBy
    private String postedBy;
    
    // User information for verifiedBy (only if status is VERIFIED or POSTED)
    private String verifiedBy;
    
    // User information for rejectedBy (only if status is DECLINED)
    private String rejectedBy;
    
    // Rejection reason (only if status is DECLINED)
    private String rejectionReason;
    
    // Part transactions
    private List<PartTranInfo> partTrans;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartTranInfo {
        private Long id;
        private Integer partTranNumber;
        private Character tranType;
        private Double amount;
        private String tranParticulars;
        private Boolean isPrimary;
    }
}
