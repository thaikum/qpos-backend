package org.example.qposbackend.DTOs;

import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Stock.stocktaking.StockTakeType;

import java.util.Date;
import java.util.Set;

public record StockTakeWithUserRequest(
    StockTakeType stockTakeType, Set<Long> ids, Date date, User user) {}
