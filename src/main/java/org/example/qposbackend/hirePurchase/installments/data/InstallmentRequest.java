package org.example.qposbackend.hirePurchase.installments.data;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.qposbackend.hirePurchase.installments.ModeOfPayment;

@Data
public class InstallmentRequest {
    @NotNull(message = "Hire purchase id cannot be null") private Long hirePurchaseId;
    @NotNull(message = "Amount cannot be null") private Double amount;
    @NotNull(message = "Mode of payment cannot be null") private ModeOfPayment modeOfPayment;
}
