package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StockOverageCause {
  UNRECORDED_DELIVERY("DELIVERED NOT RECORDED", "Delivery was not captured in system"),
  COUNTING_ERROR("PREVIOUS COUTING ERROR", "Initial stock count was incorrect"),
  SUPPLIER_OVER_SHIP("SUPPLIER OVER SHIP", "Supplier delivered more than ordered"),
  EMPLOYEE_STOCKING_PERSONAL_ITEMS(
      "EMPLOYEE STOCKING PERSONAL ITEMS", "Unauthorized personal items detected"),
  UNKNOWN("UNKNOWN", "Cause could not be determined");

  private final String displayName;
  private final String description;
}
