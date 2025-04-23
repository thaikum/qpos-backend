package org.example.qposbackend.Item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnitsOfMeasure {
    PIECES("Number of Pieces", "Number of pieces"),
    KILOGRAMS("Killograms", "Amount in killograms"),
    GRAMS("Gram", "Amount in grams"),
    METERS("Meters", "Number of meters"),
    INCHES("Inches", "Number of inches");

    private final String displayName;
    private final String description;
}
