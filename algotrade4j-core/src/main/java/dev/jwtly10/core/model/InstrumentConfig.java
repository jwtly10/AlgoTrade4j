package dev.jwtly10.core.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InstrumentConfig {
    private final String symbol;
    private final int decimalPlaces;
    private final double minimumMove;
    private final double pipValue;
    // private final minimumQuantity
    private final boolean isForex;
}