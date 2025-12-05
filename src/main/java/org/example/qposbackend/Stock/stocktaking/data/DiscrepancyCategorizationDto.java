package org.example.qposbackend.Stock.stocktaking.data;

import lombok.Data;

@Data
public class DiscrepancyCategorizationDto {
    private double quantity;
    private long reconConfigId;
    private boolean deductEmployee;
    private long employeeToDeductId;
}
