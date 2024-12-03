package dev.jwtly10.core.risk;

import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BacktestRiskManagerTest {
    private BacktestRiskManager riskManager;
    private Instrument testInstrument;

    @BeforeEach
    void setUp() {
        riskManager = new BacktestRiskManager();
        riskManager.setTimezone("UTC");
        testInstrument = Instrument.EURUSD;
    }

    @Test
    void shouldThrowExceptionWhenTimezoneNotSet() {
        BacktestRiskManager manager = new BacktestRiskManager();
        DefaultTick tick = createTick(ZonedDateTime.now(ZoneId.of("UTC")));

        assertThrows(IllegalStateException.class,
                () -> manager.checkAndSetOnTick(tick, 1000.0));
    }

    @Test
    void shouldCreateInitialEquityOnFirstTick() {
        DefaultTick tick = createTick(ZonedDateTime.now(ZoneId.of("UTC")));

        riskManager.checkAndSetOnTick(tick, 1000.0);

        assertTrue(riskManager.getCurrentDayStartingEquity().isPresent());
        assertEquals(1000.0, riskManager.getCurrentDayStartingEquity().get().lastEquity());
    }

    @Test
    void shouldNotCreateNewEquityForSameDay() {
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));
        DefaultTick firstTick = createTick(time);
        DefaultTick secondTick = createTick(time.plusHours(1));

        riskManager.checkAndSetOnTick(firstTick, 1000.0);
        riskManager.checkAndSetOnTick(secondTick, 1500.0);

        assertEquals(1000.0, riskManager.getCurrentDayStartingEquity().get().lastEquity());
    }

    @Test
    void shouldCreateNewEquityOnNewDay() {
        ZonedDateTime day1 = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime day2 = day1.plusDays(1).withHour(0).withMinute(0).withSecond(1);

        DefaultTick firstTick = createTick(day1);
        DefaultTick secondTick = createTick(day2);

        riskManager.checkAndSetOnTick(firstTick, 1000.0);
        riskManager.checkAndSetOnTick(secondTick, 1500.0);

        assertEquals(1500.0, riskManager.getCurrentDayStartingEquity().get().lastEquity());
    }

    private DefaultTick createTick(ZonedDateTime time) {
        return new DefaultTick(
                testInstrument,
                new Number(1.0),
                new Number(1.1),
                new Number(1.2),
                new Number( 1000),
                time
        );
    }
}