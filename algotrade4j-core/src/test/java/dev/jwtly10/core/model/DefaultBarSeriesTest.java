package dev.jwtly10.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static dev.jwtly10.core.model.Instrument.NAS100USD;
import static org.junit.jupiter.api.Assertions.*;

class DefaultBarSeriesTest {

    private static final int MAX_BAR_COUNT = 5;
    private DefaultBarSeries series;

    @BeforeEach
    void setUp() {
        series = new DefaultBarSeries("TestSeries", MAX_BAR_COUNT);
    }

    @Test
    void testConstructor() {
        assertEquals("TestSeries", series.getName());
        assertEquals(MAX_BAR_COUNT, series.getMaximumBarCount());
        assertEquals(0, series.getBarCount());
    }

    @Test
    void testDefaultConstructor() {
        DefaultBarSeries defaultSeries = new DefaultBarSeries(MAX_BAR_COUNT);
        assertEquals("DefaultBarSeries", defaultSeries.getName());
        assertEquals(MAX_BAR_COUNT, defaultSeries.getMaximumBarCount());
    }

    @Test
    void testAddBar() {
        Bar bar1 = createMockBar(NAS100USD, 1);
        series.addBar(bar1);
        assertEquals(1, series.getBarCount());
        assertEquals(bar1, series.getLastBar());
    }

    @Test
    void testAddBarBeyondMaximum() {
        for (int i = 0; i < MAX_BAR_COUNT + 2; i++) {
            series.addBar(createMockBar(NAS100USD, i));
        }
        assertEquals(MAX_BAR_COUNT, series.getBarCount());
        assertEquals(MAX_BAR_COUNT + 1, ((MockBar) series.getLastBar()).index);
    }

    @Test
    void testGetBar() {
        Bar bar1 = createMockBar(NAS100USD, 1);
        Bar bar2 = createMockBar(NAS100USD, 2);
        series.addBar(bar1);
        series.addBar(bar2);
        assertEquals(bar1, series.getBar(0));
        assertEquals(bar2, series.getBar(1));
    }

    @Test
    void testGetBarInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> series.getBar(0));
        series.addBar(createMockBar(NAS100USD, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> series.getBar(1));
        assertThrows(IndexOutOfBoundsException.class, () -> series.getBar(-1));
    }

    @Test
    void testGetLastBar() {
        assertNull(series.getLastBar());
        Bar bar1 = createMockBar(NAS100USD, 1);
        series.addBar(bar1);
        assertEquals(bar1, series.getLastBar());
        Bar bar2 = createMockBar(NAS100USD, 2);
        series.addBar(bar2);
        assertEquals(bar2, series.getLastBar());
    }

    @Test
    void testGetLastBars() {
        for (int i = 0; i < MAX_BAR_COUNT; i++) {
            series.addBar(createMockBar(NAS100USD, i));
        }
        BarSeries lastThree = series.getLastBars(3);
        assertEquals(3, lastThree.getBarCount());
        assertEquals(2, ((MockBar) lastThree.getBar(0)).index);
        assertEquals(4, ((MockBar) lastThree.getBar(2)).index);
    }

    @Test
    void testGetLastBarsInvalidArgument() {
        assertThrows(IllegalArgumentException.class, () -> series.getLastBars(0));
        assertThrows(IllegalArgumentException.class, () -> series.getLastBars(-1));
    }

    @Test
    void testGetLastBarsMoreThanAvailable() {
        series.addBar(createMockBar(NAS100USD, 1));
        series.addBar(createMockBar(NAS100USD, 2));
        BarSeries lastBars = series.getLastBars(3);
        assertEquals(2, lastBars.getBarCount());
    }

    private Bar createMockBar(Instrument instrument, int index) {
        return new MockBar(instrument, index);
    }

    private static class MockBar implements Bar {
        private final Instrument instrument;
        private final int index;

        public MockBar(Instrument instrument, int index) {
            this.instrument = instrument;
            this.index = index;
        }

        @Override
        public Instrument getInstrument() {
            return instrument;
        }

        @Override
        public void update(Tick tick) {
            // Not implemented for this mock
        }

        @Override
        public Duration getTimePeriod() {
            return Duration.ofMinutes(1);
        }

        @Override
        public ZonedDateTime getOpenTime() {
            return ZonedDateTime.now().minusMinutes(1);
        }

        @Override
        public ZonedDateTime getCloseTime() {
            return ZonedDateTime.now();
        }

        @Override
        public void setCloseTime(ZonedDateTime closeTime) {
            // Not implemented for this mock
        }

        @Override
        public Number getOpen() {
            return new Number(100.0);
        }

        @Override
        public Number getHigh() {
            return new Number(101.0);
        }

        @Override
        public Number getLow() {
            return new Number(99.0);
        }

        @Override
        public Number getClose() {
            return new Number(100.5);
        }

        @Override
        public Number getVolume() {
            return new Number(1000);
        }

        @Override
        public String toString() {
            return "MockBar{" +
                    "instrument='" + instrument + '\'' +
                    ", index=" + index +
                    '}';
        }
    }
}