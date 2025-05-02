package org.example.qposbackend.Stock.stocktaking.stocktakeRecon.stockTakeReconTypeConfig.stockTakeReconTypeConfigValidator;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StockTakeOverageCauseValidator.class)
public @interface ValidStockOverageCause  {}
