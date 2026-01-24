package org.example.qposbackend.Accounting.Transactions.TranHeader.mappers;

import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.TransactionStatus;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranType;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.TranHeaderResponseDTO;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class TranHeaderMapper {
    
    public static TranHeaderResponseDTO toResponseDTO(TranHeader tranHeader) {
        if (tranHeader == null) {
            return null;
        }
        
        // Calculate totalAmount - if null, use total of debits
        Double totalAmount = tranHeader.getTotalAmount();
        if (totalAmount == null && tranHeader.getPartTrans() != null) {
            totalAmount = calculateTotalDebits(tranHeader.getPartTrans());
        }
        
        TranHeaderResponseDTO.TranHeaderResponseDTOBuilder builder = TranHeaderResponseDTO.builder()
                .tranId(tranHeader.getTranId())
                .totalAmount(totalAmount)
                .status(tranHeader.getStatus())
                .postedDate(tranHeader.getPostedDate())
                .verifiedDate(tranHeader.getVerifiedDate())
                .rejectedDate(tranHeader.getRejectedDate())
                .tranCategory(tranHeader.getTranCategory())
                .description(tranHeader.getDescription())
                .postedBy(mapUserShopToUserInfo(tranHeader.getPostedBy()))
                .partTrans(mapPartTrans(tranHeader.getPartTrans()));
        
        // Set verifiedBy or rejectedBy based on status
        TransactionStatus status = tranHeader.getStatus();
        if (status == TransactionStatus.VERIFIED || status == TransactionStatus.POSTED) {
            builder.verifiedBy(mapUserShopToUserInfo(tranHeader.getVerifiedBy()));
        } else if (status == TransactionStatus.DECLINED) {
            builder.rejectedBy(mapUserShopToUserInfo(tranHeader.getRejectedBy()))
                   .rejectionReason(tranHeader.getRejectionReason());
        }
        
        return builder.build();
    }
    
    private static Double calculateTotalDebits(List<PartTran> partTrans) {
        if (partTrans == null || partTrans.isEmpty()) {
            return null;
        }
        
        return partTrans.stream()
                .filter(pt -> pt.getTranType() != null && pt.getTranType() == TranType.DEBIT)
                .filter(pt -> pt.getAmount() != null)
                .mapToDouble(PartTran::getAmount)
                .sum();
    }
    
    private static String mapUserShopToUserInfo(UserShop userShop) {
        if (userShop == null || userShop.getUser() == null) {
            return null;
        }
        
        String firstName = StringUtils.trimToEmpty(userShop.getUser().getFirstName());
        String lastName = StringUtils.trimToEmpty(userShop.getUser().getLastName());
        return StringUtils.trimToNull(firstName + " " + lastName);
    }
    
    private static List<TranHeaderResponseDTO.PartTranInfo> mapPartTrans(List<PartTran> partTrans) {
        if (partTrans == null) {
            return null;
        }
        
        return partTrans.stream()
                .map(TranHeaderMapper::mapPartTranToInfo)
                .collect(Collectors.toList());
    }
    
    private static TranHeaderResponseDTO.PartTranInfo mapPartTranToInfo(PartTran partTran) {
        if (partTran == null) {
            return null;
        }
        
        return TranHeaderResponseDTO.PartTranInfo.builder()
                .id(partTran.getId())
                .partTranNumber(partTran.getPartTranNumber())
                .tranType(partTran.getTranType())
                .amount(partTran.getAmount())
                .tranParticulars(partTran.getTranParticulars())
                .isPrimary(partTran.getIsPrimary())
                .build();
    }
}
