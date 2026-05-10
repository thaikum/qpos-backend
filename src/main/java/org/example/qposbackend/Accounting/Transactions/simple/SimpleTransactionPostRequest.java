package org.example.qposbackend.Accounting.Transactions.simple;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class SimpleTransactionPostRequest {

  @NotNull private SimpleTransactionKind kind;

  @NotNull private SimplePaymentSource paymentSource;

  @NotNull @Positive private Double amount;

  @NotBlank
  @Size(min = 3, max = 2000)
  private String particular;

  /** Defaults to shop system date server-side when null. */
  private LocalDate postedDate;
}
