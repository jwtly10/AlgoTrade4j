package dev.jwtly10.core.model;

import lombok.Getter;

/**
 * Instruments is an enum class to consolidate instruments in the system.
 * This should be extended to support new clients and their mappings of instruments, and other instrument data
 */
@Getter
public enum Instrument {
    NAS100USD("NAS100_USD", 1, 0.1, false),
    EURUSD("EUR_USD", 5, 0.00001, true),
    GBPUSD("GBP_USD", 5, 0.00001, true);

    private final String oandaSymbol;
    private final int decimalPlaces;
    private final double minimumMove;
    private final boolean isForex;

    Instrument(String oandaSymbol, int decimalPlaces, double minimumMove, boolean isForex) {
        this.oandaSymbol = oandaSymbol;
        this.decimalPlaces = decimalPlaces;
        this.minimumMove = minimumMove;
        this.isForex = isForex;
    }

    public static Instrument fromOandaSymbol(String oandaSymbol) {
        for (Instrument instrument : values()) {
            if (instrument.oandaSymbol.equals(oandaSymbol)) {
                return instrument;
            }
        }
        throw new IllegalArgumentException("No Instrument found for Oanda symbol: " + oandaSymbol);
    }

    public static Instrument fromMt5Symbol(String oandaSymbol) {
        // TODO: This needs to be supported
        return fromOandaSymbol("NAS100_USD");
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

    public double getPipValue() {
        return minimumMove;
    }
}