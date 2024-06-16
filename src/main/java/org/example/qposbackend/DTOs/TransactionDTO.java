package org.example.qposbackend.DTOs;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class TransactionDTO {
    private Long tranId;
    private Character tranType;
    private Date tranDate;
    private Double tranAmount;
    private String tranCurrency;
    private String tranParticulars;
    private String tranStatus;
    private String accountName;
    private String createdBy;
    private String updatedBy;
}
