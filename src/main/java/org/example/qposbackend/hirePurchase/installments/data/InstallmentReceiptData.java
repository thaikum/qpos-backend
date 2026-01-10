package org.example.qposbackend.hirePurchase.installments.data;

import lombok.Builder;
import lombok.Data;
import org.example.qposbackend.hirePurchase.installments.Installment;

import java.util.List;

@Data
@Builder
public class InstallmentReceiptData {
    private Installment installment;
    private String shopName;
    private String cashierName;
    private String receiptNumber;
    private Double amountRemaining;
    private Double totalExpectedAmount;
    private Double totalPaidAmount;
    private List<String> itemNames;
}
