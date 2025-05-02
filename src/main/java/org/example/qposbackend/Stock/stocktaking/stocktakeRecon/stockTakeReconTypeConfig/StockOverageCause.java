package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StockOverageCause {
  UNRECORDED_DELIVERY("Delivery was not captured in system"),
  COUNTING_ERROR("Initial stock count was incorrect"),
  SUPPLIER_OVER_SHIP("Supplier delivered more than ordered"),
  EMPLOYEE_STOCKING_PERSONAL_ITEMS("Unauthorized personal items detected"),
  UNKNOWN("Cause could not be determined");

  private final String description;
}
