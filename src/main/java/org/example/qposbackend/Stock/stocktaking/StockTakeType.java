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
  FULL("ALL ITEMS", "ALL ITEMS"),
  /** High-value items (ABC — roughly top 80% of stock value) */
  ABC_CLASS_A("ABC — A items (high value)", "Count A-class (highest stock value) items only"),
  /** Medium-value items (ABC — next ~15% of stock value) */
  ABC_CLASS_B("ABC — B items (medium value)", "Count B-class items only"),
  /** Lower-value items (ABC — remaining ~5% of stock value) */
  ABC_CLASS_C("ABC — C items (lower value)", "Count C-class items only");

  private final String displayName;
  private final String description;
}
