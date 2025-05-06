package org.example.qposbackend.Stock.stocktaking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StockTakeType {
  CATEGORY("CATEGORY", "CATEGORY"),
  ITEMS("SELECTED ITEMS", "SELECTED ITEMS"),
  SUB_CATEGORY("SUB CATEGORY", "SUB CATEGORY"),
  RANDOM("RANDOM ITEMS", "RANDOM ITEMS"),
  FULL("ALL ITEMS", "ALL ITEMS");

  private final String displayName;
  private final String description;
}
