package org.example.qposbackend.DTOs;

import org.example.qposbackend.Stock.stocktaking.StockTakeType;

import java.util.Date;
import java.util.Set;

public record StockTakeRequest(StockTakeType stockTakeType, Set<Long> ids, Date date) {}
