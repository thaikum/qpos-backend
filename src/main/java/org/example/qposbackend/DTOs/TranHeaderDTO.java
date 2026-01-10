package org.example.qposbackend.DTOs;

import java.time.LocalDate;
import java.util.List;

public record TranHeaderDTO(
        LocalDate postedDate,
        List<PartTranDTO> partTrans
) {}
