package org.example.qposbackend.Accounting.Transactions.TranHeader.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SimpleJournal {
    private List<SimpleJournalLine> journalLines;
    private String particulars;
}
