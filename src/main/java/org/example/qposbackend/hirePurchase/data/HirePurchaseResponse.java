package org.example.qposbackend.hirePurchase.data;

import lombok.Builder;
import lombok.Data;
import org.example.qposbackend.hirePurchase.installments.Installment;
import org.example.qposbackend.order.orderItem.data.OrderItemResponse;

import java.util.List;

@Data
@Builder
public class HirePurchaseResponse {
  private Long id;
  private String customerName;
  private Long customerId;
  private List<Installment> installments;
  private Double totalPaidAmount;
  private Double remainingAmount;
  private Double expectedTotalPay;
  private List<OrderItemResponse> orderItems;
}
