package dev.jwtly10.core.model;

import lombok.Getter;

/**
 * Instruments is an enum class to consolidate instruments in the system.
 * This should be extended to support new clients and their mappings of instruments
 */
@Getter
public enum Instrument {
    NAS100USD("NAS100_USD"),
    EURUSD("EUR_USD"),
    GBPUSD("GBP_USD");

    private final String oandaSymbol;

    Instrument(String oandaSymbol) {
        this.oandaSymbol = oandaSymbol;
    }

    /**
     * Convert a given oanda symbol to our internal instrument
     *
     * @param oandaSymbol the symbol from oandas client
     * @return the internal enum instrument value
     */
    public static Instrument fromOandaSymbol(String oandaSymbol) {
        for (Instrument instrument : values()) {
            if (instrument.oandaSymbol.equals(oandaSymbol)) {
                return instrument;
            }
        }
        throw new IllegalArgumentException("No Instrument found for Oanda symbol: " + oandaSymbol);
    }
}