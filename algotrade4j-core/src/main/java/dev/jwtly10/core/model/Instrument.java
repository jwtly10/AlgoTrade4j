package dev.jwtly10.core.model;

import lombok.Getter;

/**
 * Instruments is an enum class to consolidate instruments in the system.
 * This should be extended to support new clients and their mappings of instruments, and other instrument data
 */
@Getter
public enum Instrument {
    NAS100USD("NAS100_USD", 2, 1.0),
    EURUSD("EUR_USD", 5, 0.00001),
    GBPUSD("GBP_USD", 5, 0.00001);

    private final String oandaSymbol;
    private final int decimalPlaces;
    private final double minimumMove;

    Instrument(String oandaSymbol, int decimalPlaces, double minimumMove) {
        this.oandaSymbol = oandaSymbol;
        this.decimalPlaces = decimalPlaces;
        this.minimumMove = minimumMove;
    }

    public static Instrument fromOandaSymbol(String oandaSymbol) {
        for (Instrument instrument : values()) {
            if (instrument.oandaSymbol.equals(oandaSymbol)) {
                return instrument;
            }
        }
        throw new IllegalArgumentException("No Instrument found for Oanda symbol: " + oandaSymbol);
    }

    public static InstrumentData[] getAllInstrumentData() {
        return java.util.Arrays.stream(values())
                .map(Instrument::getInstrumentData)
                .toArray(InstrumentData[]::new);
    }

    public InstrumentData getInstrumentData() {
        return new InstrumentData(
                this.name(),
                this.oandaSymbol,
                this.decimalPlaces,
                this.minimumMove
        );
    }
}