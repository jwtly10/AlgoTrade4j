package dev.jwtly10.core.indicators;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.IndicatorEvent;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class iATRTest {

    private iATR iAtrIndicator;
    private EventPublisher mockEventPublisher;

    @BeforeEach
    void setUp() {
        mockEventPublisher = mock(EventPublisher.class);
        iAtrIndicator = new iATR(14);
        iAtrIndicator.setEventPublisher(mockEventPublisher);
        iAtrIndicator.setStrategyId("TESTING");
    }

    @Test
    void testInitialState() {
        assertEquals("ATR 14", iAtrIndicator.getName());
        assertEquals(14, iAtrIndicator.getRequiredPeriods());
        assertFalse(iAtrIndicator.isReady());
        assertEquals(0, iAtrIndicator.getValue());
    }

    @Test
    void testUpdate() {
        Bar bar1 = createMockBar(100, 90, 95);
        Bar bar2 = createMockBar(105, 85, 100);
        Bar bar3 = createMockBar(110, 90, 105);

        iAtrIndicator.update(bar1);

        assertFalse(iAtrIndicator.isReady());
        assertEquals(0, iAtrIndicator.getValue());

        iAtrIndicator.update(bar2);
        assertFalse(iAtrIndicator.isReady());
        assertEquals(0, iAtrIndicator.getValue());

        // Update until ready
        for (int i = 0; i < 12; i++) {
            iAtrIndicator.update(bar3);
        }

        assertTrue(iAtrIndicator.isReady());
        assertNotEquals(0, iAtrIndicator.getValue());

        verify(mockEventPublisher, times(14)).publishEvent(argThat(event ->
                event instanceof IndicatorEvent
        ));
    }

    @Test
    void testATRAccuracy() {
        int period = 14;
        iATR atr = new iATR(period);

        double[] highs = {100, 102, 104, 103, 105, 107, 108, 107, 108, 109, 110, 112, 111, 113, 114, 115, 116, 117, 118, 119};
        double[] lows = {98, 99, 100, 101, 102, 103, 104, 105, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115};
        double[] closes = {99, 101, 102, 102, 104, 105, 106, 106, 107, 108, 109, 110, 109, 111, 112, 113, 114, 115, 116, 117};

        double[] trueRanges = new double[highs.length];
        double[] expectedATR = new double[highs.length];
        double[] actualATR = new double[highs.length];

        for (int i = 0; i < highs.length; i++) {
            if (i == 0) {
                trueRanges[i] = highs[i] - lows[i];
            } else {
                double highLow = highs[i] - lows[i];
                double highPrevClose = Math.abs(highs[i] - closes[i - 1]);
                double lowPrevClose = Math.abs(lows[i] - closes[i - 1]);
                trueRanges[i] = Math.max(highLow, Math.max(highPrevClose, lowPrevClose));
            }

            Bar bar = createMockBar(highs[i], lows[i], closes[i]);
            atr.update(bar);
            actualATR[i] = atr.getValue();

            expectedATR[i] = calculateExpectedATR(trueRanges, i, period);

            System.out.printf("%5d | %10.4f | %10.4f | %10.4f%n", i, trueRanges[i], expectedATR[i], actualATR[i]);
        }

        assertEquals(expectedATR[expectedATR.length - 1], actualATR[actualATR.length - 1], 0.0001,
                "Final ATR value should be equal");
    }

    private double calculateExpectedATR(double[] trueRanges, int currentIndex, int period) {
        if (currentIndex < period - 1) {
            return 0;  // Return 0 for the first 'period - 1' values
        } else {
            return trueRanges[currentIndex];  // Return the current true range as ATR
        }
    }

    @Test
    void testGetValueOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> iAtrIndicator.getValue(0));
        assertThrows(IndexOutOfBoundsException.class, () -> iAtrIndicator.getValue(-1));
    }

    @Test
    void testATRCalculation() {
        Bar bar1 = createMockBar(100, 90, 95);
        Bar bar2 = createMockBar(105, 85, 100);

        iAtrIndicator.update(bar1);
        iAtrIndicator.update(bar2);

        for (int i = 0; i < 12; i++) {
            iAtrIndicator.update(createMockBar(100, 90, 95));
        }

        assertTrue(iAtrIndicator.isReady());
        double atr = iAtrIndicator.getValue();
        assertTrue(atr > 0);
        assertTrue(atr < 20.0 || atr == 20.0);
    }

    private Bar createMockBar(double high, double low, double close) {
        Bar mockBar = Mockito.mock(Bar.class);
        when(mockBar.getHigh()).thenReturn(new Number(BigDecimal.valueOf(high).setScale(2, RoundingMode.HALF_UP)));
        when(mockBar.getLow()).thenReturn(new Number(BigDecimal.valueOf(low).setScale(2, RoundingMode.HALF_UP)));
        when(mockBar.getClose()).thenReturn(new Number(BigDecimal.valueOf(close).setScale(2, RoundingMode.HALF_UP)));
        when(mockBar.getInstrument()).thenReturn(Instrument.NAS100USD);
        when(mockBar.getOpenTime()).thenReturn(ZonedDateTime.now());
        return mockBar;
    }
}