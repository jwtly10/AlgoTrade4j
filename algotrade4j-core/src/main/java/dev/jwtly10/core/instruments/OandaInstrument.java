package dev.jwtly10.core.instruments;

public enum OandaInstrument {
    // Stock Indices
    NAS100_USD("NAS100_USD", "US TECH 100"),
    SPX500_USD("SPX500_USD", "US SPX 500"),
    US30_USD("US30_USD", "US Wall St 30"),

    // Forex Pairs
    EUR_USD("EUR_USD", "Euro/US Dollar"),
    GBP_USD("GBP_USD", "British Pound/US Dollar"),
    USD_JPY("USD_JPY", "US Dollar/Japanese Yen"),

    // Commodities
    XAU_USD("XAU_USD", "Gold/US Dollar"),
    BCO_USD("BCO_USD", "Brent Crude Oil");

    private final String instrumentName;
    private final String description;

    OandaInstrument(String instrumentName, String description) {
        this.instrumentName = instrumentName;
        this.description = description;
    }

    public static OandaInstrument fromString(String text) {
        for (OandaInstrument instrument : OandaInstrument.values()) {
            if (instrument.instrumentName.equalsIgnoreCase(text)) {
                return instrument;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return instrumentName;
    }
}