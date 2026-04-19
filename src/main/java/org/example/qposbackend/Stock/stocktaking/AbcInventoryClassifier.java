package org.example.qposbackend.Stock.stocktaking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.qposbackend.InventoryItem.InventoryItem;

/**
 * ABC analysis by current stock value (qty × selling price). Used to pick which SKUs to include in
 * cycle-count style stock takes: A = highest-value band (~top 80% of value), B = next ~15%, C =
 * remainder.
 */
public final class AbcInventoryClassifier {

  public enum AbcClass {
    A,
    B,
    C
  }

  private AbcInventoryClassifier() {}

  public static Map<Long, AbcClass> classifyByStockValue(List<InventoryItem> items) {
    Map<Long, AbcClass> result = new HashMap<>();
    if (items == null || items.isEmpty()) {
      return result;
    }
    double totalValue =
        items.stream().mapToDouble(AbcInventoryClassifier::stockValue).filter(v -> v > 0).sum();
    if (totalValue <= 0) {
      for (InventoryItem ii : items) {
        result.put(ii.getId(), AbcClass.C);
      }
      return result;
    }
    List<InventoryItem> sorted =
        items.stream()
            .sorted(Comparator.comparingDouble(AbcInventoryClassifier::stockValue).reversed())
            .toList();
    double running = 0;
    for (InventoryItem item : sorted) {
      double v = stockValue(item);
      AbcClass cls;
      if (running < 0.80 * totalValue) {
        cls = AbcClass.A;
      } else if (running < 0.95 * totalValue) {
        cls = AbcClass.B;
      } else {
        cls = AbcClass.C;
      }
      result.put(item.getId(), cls);
      running += v;
    }
    return result;
  }

  private static double stockValue(InventoryItem ii) {
    double q = ii.getQuantity() == null ? 0D : ii.getQuantity();
    double p = ii.getSellingPrice() == null ? 0D : ii.getSellingPrice();
    return q * p;
  }
}
