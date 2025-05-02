package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.stockTakeReconTypeConfigValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconType;
import org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.StockTakeReconTypeConfig;

public class StockTakeOverageCauseValidator
    implements ConstraintValidator<ValidStockOverageCause, StockTakeReconTypeConfig> {

  @Override
  public boolean isValid(StockTakeReconTypeConfig value, ConstraintValidatorContext context) {
    return value.getStockTakeReconType() != StockTakeReconType.EXCESS_ITEMS
            && value.getStockOverageCause() == null
        || value.getStockTakeReconType() == StockTakeReconType.EXCESS_ITEMS
            && value.getStockOverageCause() != null;
  }
}
