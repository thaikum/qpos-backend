package org.example.qposbackend.DTOs;

import java.util.Date;
import java.util.List;

public record TranHeaderDTO(
        Date postedDate,
        List<PartTranDTO> partTrans
) {}
