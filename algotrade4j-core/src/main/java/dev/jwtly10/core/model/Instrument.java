package dev.jwtly10.core.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static dev.jwtly10.core.model.Broker.MT5_FTMO;
import static dev.jwtly10.core.model.Broker.OANDA;


/**
 * Instruments is an enum class to consolidate instruments in the system.
 * This should be extended to support new clients and their mappings of instruments, and other instrument data
 * <p>
 * Here is the source of truth for symbols supported by the platform. We define broker implementations here
 * along with any metadata required during the execution of trades
 */
@Getter
public enum Instrument {
    NAS100USD(new HashMap<>() {{
        put(OANDA, InstrumentConfig.builder()
                .symbol("NAS100_USD")
                .decimalPlaces(1)
                .quantityPrecision(1)
                .minimumMove(0.1)
                .pipValue(0.1)
                .isForex(false)
                .build());
        put(MT5_FTMO, InstrumentConfig.builder()
                .symbol("US100.cash")
                .decimalPlaces(1)
                .minimumMove(0.1)
                .quantityPrecision(2)
                .isForex(false)
                .pipValue(0.1)
                .build());
    }}),
    EURUSD(new HashMap<>() {{
        put(OANDA, InstrumentConfig.builder()
                .symbol("EUR_USD")
                .decimalPlaces(5)
                .minimumMove(0.00001)
                .quantityPrecision(1)
                .pipValue(0.00001)
                .isForex(true)
                .build());
        put(MT5_FTMO, InstrumentConfig.builder()
                .symbol("EURUSD")
                .decimalPlaces(5)
                .minimumMove(0.00001)
                .quantityPrecision(2)
                .pipValue(0.00001)
                .isForex(true)
                .build());
    }}),
    GBPUSD(new HashMap<>() {{
        put(OANDA, InstrumentConfig.builder()
                .symbol("GBP_USD")
                .decimalPlaces(5)
                .minimumMove(0.00001)
                .quantityPrecision(1)
                .pipValue(0.00001)
                .isForex(true)
                .build());
        put(MT5_FTMO, InstrumentConfig.builder()
                .symbol("GBPUSD")
                .decimalPlaces(5)
                .minimumMove(0.00001)
                .quantityPrecision(2)
                .pipValue(0.00001)
                .isForex(true)
                .build());
    }});

    private final Map<Broker, InstrumentConfig> brokerConfigs;

    Instrument(Map<Broker, InstrumentConfig> brokerConfigs) {
        this.brokerConfigs = brokerConfigs;
    }

    public static Instrument fromBrokerSymbol(Broker broker, String symbol) {
        for (Instrument instrument : values()) {
            InstrumentConfig config = instrument.brokerConfigs.get(broker);
            if (config != null && config.getSymbol().equals(symbol)) {
                return instrument;
            }
        }
        throw new IllegalArgumentException("No Instrument found for broker: " + broker + " with symbol: " + symbol);
    }

    public InstrumentConfig getBrokerConfig(Broker brokerName) {
        InstrumentConfig config = this.brokerConfigs.get(brokerName);
        if (config == null) {
            throw new IllegalArgumentException("No config found for broker: " + brokerName);
        }
        return config;
    }

    public static InstrumentData[] getChartingInstrumentData() {
        // TODO: We essentially only use OANDA for charting data, so this is hardcoded to provider
        // instrument data for OANDA only. This should be extended to support other brokers, but only if needed
        return java.util.Arrays.stream(values())
                .map(
                        instrument -> new InstrumentData(
                                instrument.name(),
                                instrument.getBrokerConfig(OANDA).getSymbol(),
                                instrument.getBrokerConfig(OANDA).getDecimalPlaces(),
                                instrument.getBrokerConfig(OANDA).getMinimumMove()
                        )
                )
                .toArray(InstrumentData[]::new);
    }
}