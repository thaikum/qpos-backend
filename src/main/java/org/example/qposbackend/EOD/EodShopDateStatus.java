package org.example.qposbackend.EOD;

import java.time.LocalDate;

/**
 * Operational "books" date for the current shop (last EOD date + 1 day, or today if no EOD),
 * compared to the shop timezone calendar date.
 */
public record EodShopDateStatus(
    LocalDate systemDate,
    LocalDate shopCalendarDate,
    LocalDate lastClosedEodDate,
    boolean needsEod) {}
