package dev.jwtly10.core.event.async;

import dev.jwtly10.core.model.IndicatorValue;
import dev.jwtly10.core.model.Instrument;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AsyncIndicatorsEventTest {

    @Test
    void testConstructorAndGetters() {
        String strategyId = "testStrategy";
        Instrument instrument = Instrument.NAS100USD;
        Map<String, List<IndicatorValue>> indicators = new HashMap<>();
        indicators.put("SMA", Arrays.asList(new IndicatorValue(1.0, ZonedDateTime.now()), new IndicatorValue(2.0, ZonedDateTime.now())));

        AsyncIndicatorsEvent event = new AsyncIndicatorsEvent(strategyId, instrument, indicators);

        assertEquals(strategyId, event.getStrategyId());
        assertEquals("ALL_INDICATORS", event.getType());
        assertEquals(instrument, event.getInstrument());
        assertEquals(indicators, event.getIndicators());
    }

    @Test
    void testFilterZeroValues() {
        String strategyId = "testStrategy";
        Instrument instrument = Instrument.NAS100USD;
        Map<String, List<IndicatorValue>> indicators = new HashMap<>();
        indicators.put("SMA", Arrays.asList(
                new IndicatorValue(1.0, ZonedDateTime.now()),
                new IndicatorValue(0.0, ZonedDateTime.now()),
                new IndicatorValue(2.0, ZonedDateTime.now())
        ));
        indicators.put("EMA", Arrays.asList(
                new IndicatorValue(0.0, ZonedDateTime.now()),
                new IndicatorValue(0.0, ZonedDateTime.now())
        ));

        AsyncIndicatorsEvent event = new AsyncIndicatorsEvent(strategyId, instrument, indicators);

        Map<String, List<IndicatorValue>> filteredIndicators = event.getIndicators();

        assertTrue(filteredIndicators.containsKey("SMA"));
        assertFalse(filteredIndicators.containsKey("EMA"));
        assertEquals(2, filteredIndicators.get("SMA").size());
        assertEquals(1.0, filteredIndicators.get("SMA").get(0).getValue());
        assertEquals(2.0, filteredIndicators.get("SMA").get(1).getValue());
    }

    @Test
    void testEmptyIndicators() {
        String strategyId = "testStrategy";
        Instrument instrument = Instrument.NAS100USD;
        Map<String, List<IndicatorValue>> indicators = new HashMap<>();

        AsyncIndicatorsEvent event = new AsyncIndicatorsEvent(strategyId, instrument, indicators);

        assertTrue(event.getIndicators().isEmpty());
    }

    @Test
    void testAllZeroValues() {
        String strategyId = "testStrategy";
        Instrument instrument = Instrument.NAS100USD;
        Map<String, List<IndicatorValue>> indicators = new HashMap<>();
        indicators.put("SMA", Arrays.asList(
                new IndicatorValue(0.0, ZonedDateTime.now()),
                new IndicatorValue(0.0, ZonedDateTime.now())
        ));

        AsyncIndicatorsEvent event = new AsyncIndicatorsEvent(strategyId, instrument, indicators);

        assertTrue(event.getIndicators().isEmpty());
    }
}