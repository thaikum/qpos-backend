package org.example.qposbackend.Stock.stocktaking.data;

import lombok.Data;

@Data
public class DiscrepancyCategorizationDto {
  private double quantity;
  private long reconConfigId;
  private boolean deductEmployee;
  /** {@link org.example.qposbackend.Authorization.User.userShop.UserShop} id */
  private Long employeeToDeductId;
}
