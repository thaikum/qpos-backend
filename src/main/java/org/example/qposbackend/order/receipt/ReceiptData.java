package org.example.qposbackend.order.receipt;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReceiptData {
    private String shopName;
    private String shopTagline;
    private String receiptNo;
    private String shopPhone;
    private String orderNo;
    private List<ReceiptItem> receiptItems;
    private Double receiptTotal;
}
