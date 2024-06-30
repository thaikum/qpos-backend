package org.example.qposbackend.Accounting.Reports.Data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DateWithAccount extends DatesData{
    private String accountNumber;
}
