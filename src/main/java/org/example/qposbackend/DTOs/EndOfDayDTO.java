package org.example.qposbackend.DTOs;

public record EndOfDayDTO(Double balanceBroughtDownCash, Double balanceBroughtDownMobile, Double totalDebtors, Double totalRecoveredDebt) {
}
