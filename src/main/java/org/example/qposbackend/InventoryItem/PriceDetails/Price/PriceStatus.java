package org.example.qposbackend.InventoryItem.PriceDetails.Price;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@RequiredArgsConstructor
public enum PriceStatus {
  ACTIVE("Active", "This price is active for use"),
  FUTURE("Future", "The next price to be used"),
  STOPPED("Stopped", "This price has been stopped, and might be reactivated"),
  EXPIRED("Expired", "This price has expired. It cannot be reactivated");

  private final String displayName;
  private final String description;
}
