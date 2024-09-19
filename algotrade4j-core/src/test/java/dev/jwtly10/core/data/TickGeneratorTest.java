package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TickGeneratorTest {

    private Instrument instrument;
    private Bar testBar;

    @BeforeEach
    void setUp() {
        instrument = Instrument.EURUSD;
        ZonedDateTime now = ZonedDateTime.now();
        Duration timePeriod = Duration.ofMinutes(5);
        testBar = new DefaultBar(instrument, timePeriod, now, new Number("1.10000"), new Number("1.10100"),
                new Number("1.09900"), new Number("1.10050"), new Number("1000"));
    }

    @Test
    void testSpreadIsCalculatedCorrectlyForShortPipValues() {
        TickGenerator generator = new TickGenerator(50, Instrument.NAS100USD, Duration.ofMinutes(5), 42L);
        List<DefaultTick> ticks = new ArrayList<>();
        DefaultBar bar = new DefaultBar(Instrument.NAS100USD, Duration.ofMinutes(5), ZonedDateTime.now(),
                new Number("15000"), new Number("10010"), new Number("9990"), new Number("10000"), new Number("69"));
        generator.generateTicks(bar, DataSpeed.INSTANT, ticks::add);

        assertEquals(40, ticks.size());

        System.out.println(Instrument.NAS100USD + ": Bid: " + ticks.getFirst().getBid() + ", Ask: " + ticks.getFirst().getAsk() + ", Spread: " + ticks.getFirst().getAsk().subtract(ticks.getFirst().getBid()));

        assertEquals(new Number("5.0000"), ticks.getFirst().getAsk().subtract(ticks.getFirst().getBid()));
        assertEquals(new Number("14997.5000"), ticks.getFirst().getBid());
        assertEquals(new Number("15002.5000"), ticks.getFirst().getAsk());

        TickGenerator generator1 = new TickGenerator(50, Instrument.GBPUSD, Duration.ofMinutes(5), 42L);
        List<DefaultTick> ticks1 = new ArrayList<>();
        DefaultBar bar1 = new DefaultBar(Instrument.GBPUSD, Duration.ofMinutes(5), ZonedDateTime.now(),
                new Number("1.2000"), new Number("1.2400"), new Number("1.1800"), new Number("1.2000"), new Number("69"));
        generator1.generateTicks(bar1, DataSpeed.INSTANT, ticks1::add);

        assertEquals(40, ticks1.size());

        System.out.println(Instrument.GBPUSD + ": Bid: " + ticks1.getFirst().getBid() + ", Ask: " + ticks1.getFirst().getAsk() + ", Spread: " + ticks1.getFirst().getAsk().subtract(ticks1.getFirst().getBid()));
        assertEquals(new Number("0.00050"), ticks1.getFirst().getAsk().subtract(ticks1.getFirst().getBid()));
        assertEquals(new Number("1.19975"), ticks1.getFirst().getBid());
        assertEquals(new Number("1.20025"), ticks1.getFirst().getAsk());
    }


    @Test
    void testConstructorWithExplicitTicksPerBar() {
        TickGenerator generator = new TickGenerator(100, instrument, 10, Duration.ofMinutes(5), 42L);
        List<DefaultTick> ticks = generateTicks(generator);
        assertEquals(100, ticks.size());
    }

    @ParameterizedTest
    @CsvSource({
            "PT1M, 20",
            "PT5M, 40",
            "PT15M, 60",
            "PT30M, 80",
            "PT1H, 100",
            "PT4H, 150",
            "P1D, 200"
    })
    void testConstructorWithPeriodMapping(Duration period, int expectedTicks) {
        TickGenerator generator = new TickGenerator(10, instrument, period, 42L);
        List<DefaultTick> ticks = generateTicks(generator);
        assertEquals(expectedTicks, ticks.size());
    }

    @Test
    void testTickGeneration() {
        TickGenerator generator = new TickGenerator(10, instrument, Duration.ofMinutes(5), 42L);
        List<DefaultTick> ticks = generateTicks(generator);

        assertEquals(40, ticks.size());
        assertEquals(testBar.getOpen(), ticks.get(0).getMid());
        assertEquals(testBar.getClose(), ticks.get(ticks.size() - 1).getMid());

        boolean hitHigh = false;
        boolean hitLow = false;
        for (DefaultTick tick : ticks) {
            assertTrue(tick.getMid().compareTo(testBar.getLow()) >= 0);
            assertTrue(tick.getMid().compareTo(testBar.getHigh()) <= 0);
            if (tick.getMid().equals(testBar.getHigh())) hitHigh = true;
            if (tick.getMid().equals(testBar.getLow())) hitLow = true;
        }
        assertTrue(hitHigh, "High price was not hit");
        assertTrue(hitLow, "Low price was not hit");
    }

    @Test
    void testTimeDistribution() {
        TickGenerator generator = new TickGenerator(10, instrument, Duration.ofMinutes(5), 42L);
        List<DefaultTick> ticks = generateTicks(generator);

        ZonedDateTime startTime = testBar.getOpenTime();
        ZonedDateTime endTime = startTime.plus(Duration.ofMinutes(5));

        for (int i = 0; i < ticks.size(); i++) {
            DefaultTick tick = ticks.get(i);
            assertTrue(tick.getDateTime().isAfter(startTime) || tick.getDateTime().equals(startTime));
            assertTrue(tick.getDateTime().isBefore(endTime));

            if (i == ticks.size() - 1) {
                assertEquals(1, Duration.between(tick.getDateTime(), endTime).getSeconds());
            }
        }
    }

    @Test
    void testVolumeDistribution() {
        TickGenerator generator = new TickGenerator(10, instrument, Duration.ofMinutes(5), 42L);
        List<DefaultTick> ticks = generateTicks(generator);

        Number totalVolume = new Number("0");
        for (DefaultTick tick : ticks) {
            totalVolume = totalVolume.add(tick.getVolume());
        }

        System.out.println(totalVolume);
        System.out.println(testBar.getVolume());

        // Volume should be a random distribution, but always sum to the total volume of the bar
        for (DefaultTick tick : ticks) {
            assertTrue(tick.getVolume().compareTo(new Number("0")) > 0);
        }
        assertEquals(testBar.getVolume(), totalVolume);
    }

    @Test
    void testMinimumTicksPerBar() {
        TickGenerator generator = new TickGenerator(4, instrument, 10, Duration.ofMinutes(5), 42L);
        List<DefaultTick> ticks = generateTicks(generator);

        assertEquals(4, ticks.size());
        assertEquals(testBar.getOpen(), ticks.get(0).getMid());
        assertEquals(testBar.getHigh(), ticks.get(1).getMid());
        assertEquals(testBar.getLow(), ticks.get(2).getMid());
        assertEquals(testBar.getClose(), ticks.get(3).getMid());
    }

    @Test
    void testInvalidTicksPerBar() {
        assertThrows(IllegalArgumentException.class, () ->
                new TickGenerator(3, instrument, 10, Duration.ofMinutes(5), 42L));
    }

    private List<DefaultTick> generateTicks(TickGenerator generator) {
        List<DefaultTick> ticks = new ArrayList<>();
        generator.generateTicks(testBar, DataSpeed.FAST, ticks::add);
        return ticks;
    }
}