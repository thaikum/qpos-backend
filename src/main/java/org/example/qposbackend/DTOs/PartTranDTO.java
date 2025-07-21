package org.example.qposbackend.DTOs;

public record PartTranDTO(
        Character tranType,
        Double amount,
        String tranParticulars,
        Long shopAccountId
) {}
