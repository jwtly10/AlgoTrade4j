package dev.jwtly10.core.model;

import lombok.Getter;

@Getter
public enum Instrument {
    NAS100USD("NAS100_USD"),
    EURUSD("EUR_USD"),
    GBPUSD("GBP_USD");

    private final String oandaSymbol;

    Instrument(String oandaSymbol) {
        this.oandaSymbol = oandaSymbol;
    }
}