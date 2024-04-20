package org.example.qposbackend.DTOs;

import java.util.Date;
import java.util.List;

public record StockDTO(Date purchaseDate, Date arrivalDate, Double transportCost,
                       Double otherCostsIncurred, List<StockItemDTO> items){}