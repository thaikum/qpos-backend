package org.example.qposbackend.DTOs;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.example.qposbackend.Authorization.User.User;

@Data
@Builder
public class StockTakeDTO {
    private Long stockTakeId;
    private Date stockTakeDate;
    private User stockTaker;
    private List<StockTakeItemDTO> stockTakeItems;
}
