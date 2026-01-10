package org.example.qposbackend.Accounting.Transactions.TranHeader.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleJournalLine {
    private String accountName;
    private Double amount;
    private Character tranType;
}
