package org.example.qposbackend.hirePurchase.data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;
import org.example.qposbackend.hirePurchase.installments.ModeOfPayment;
import org.example.qposbackend.order.orderItem.data.OrderItemRequest;

@Data
public class HirePurchaseRequest {
  private List<OrderItemRequest> orderItems;
  private LocalDate expectedCompletionDate;

  @NotNull(message = "Initial payment must be provided.")
  @Min(value = 0, message = "Amount must be greater than 0!")
  private Double initialPayment;

  @NotNull(message = "A customer must be selected!")
  private Long customerId;

  @NotNull(message = "You must provide the mode of payment for the initial payment.")
  private ModeOfPayment modeOfPayment;

  private Double discountAllowed;
  private boolean itemReleased;
}
