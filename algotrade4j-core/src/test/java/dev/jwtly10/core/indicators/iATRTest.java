package dev.jwtly10.core.indicators;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.IndicatorEvent;
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
        assertEquals(Number.ZERO, iAtrIndicator.getValue());
    }

    @Test
    void testUpdate() {
        Bar bar1 = createMockBar(100, 90, 95);
        Bar bar2 = createMockBar(105, 85, 100);
        Bar bar3 = createMockBar(110, 90, 105);

        iAtrIndicator.update(bar1);

        assertFalse(iAtrIndicator.isReady());
        assertEquals(Number.ZERO, iAtrIndicator.getValue());

        iAtrIndicator.update(bar2);
        assertFalse(iAtrIndicator.isReady());
        assertEquals(Number.ZERO, iAtrIndicator.getValue());

        // Update until ready
        for (int i = 0; i < 12; i++) {
            iAtrIndicator.update(bar3);
        }

        assertTrue(iAtrIndicator.isReady());
        assertNotEquals(Number.ZERO, iAtrIndicator.getValue());

        verify(mockEventPublisher, times(14)).publishEvent(argThat(event ->
                event instanceof IndicatorEvent
        ));
    }

    @Test
    void testGetValue() {
        // Create a sequence of bars with varying high-low ranges
        for (int i = 0; i < 20; i++) {
            double high = 100 + i * 2;
            double low = 90 + i;
            double close = 95 + i * 1.5;
            Bar bar = createMockBar(high, low, close);
            iAtrIndicator.update(bar);
        }

        verify(mockEventPublisher, times(20)).publishEvent(argThat(event ->
                event instanceof IndicatorEvent
        ));
        assertTrue(iAtrIndicator.isReady());
        assertNotEquals(Number.ZERO, iAtrIndicator.getValue());

        Number currentATR = iAtrIndicator.getValue();

        // Check that the most recent ATR value is different from the previous one
        assertNotEquals(iAtrIndicator.getValue(), iAtrIndicator.getValue(1));

        double maxTrueRange = 0;
        for (int i = 1; i < 20; i++) {
            double highLow = (100 + i * 2) - (90 + i);
            double highPrevClose = Math.abs((100 + i * 2) - (95 + (i - 1) * 1.5));
            double lowPrevClose = Math.abs((90 + i) - (95 + (i - 1) * 1.5));
            maxTrueRange = Math.max(maxTrueRange, Math.max(highLow, Math.max(highPrevClose, lowPrevClose)));
        }

        assertTrue(currentATR.isLessThan(new Number(String.valueOf(maxTrueRange))) || currentATR.isEquals(new Number(String.valueOf(maxTrueRange))));
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
        Number atr = iAtrIndicator.getValue();
        assertTrue(atr.isGreaterThan(Number.ZERO));
        assertTrue(atr.isLessThan(new Number("20.00")) || atr.isEquals(new Number("20.00")));
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