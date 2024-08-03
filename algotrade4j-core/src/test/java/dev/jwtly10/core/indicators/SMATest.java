package dev.jwtly10.core.indicators;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.Number;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.IndicatorEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class SMATest {

    private SMA smaIndicator;
    private EventPublisher mockEventPublisher;


    @BeforeEach
    void setUp() {
        mockEventPublisher = mock(EventPublisher.class);
        smaIndicator = new SMA("SMATest", 3, mockEventPublisher);
    }

    @Test
    void testInitialState() {
        assertEquals("SMA 3", smaIndicator.getName());
        assertEquals(3, smaIndicator.getRequiredPeriods());
        assertFalse(smaIndicator.isReady());
        assertEquals(Number.ZERO, smaIndicator.getValue());
    }

    @Test
    void testUpdate() {
        Bar bar1 = createMockBar(10);
        Bar bar2 = createMockBar(20);
        Bar bar3 = createMockBar(30);

        smaIndicator.update(bar1);

        assertFalse(smaIndicator.isReady());
        assertEquals(Number.ZERO, smaIndicator.getValue());

        smaIndicator.update(bar2);
        assertFalse(smaIndicator.isReady());
        assertEquals(Number.ZERO, smaIndicator.getValue());

        smaIndicator.update(bar3);
        assertTrue(smaIndicator.isReady());
        assertEquals(new Number("20.00"), smaIndicator.getValue());

        verify(mockEventPublisher, times(3)).publishEvent(argThat(event ->
                event instanceof IndicatorEvent
        ));
    }

    @Test
    void testGetValue() {
        Bar bar1 = createMockBar(10);
        Bar bar2 = createMockBar(20);
        Bar bar3 = createMockBar(30);
        Bar bar4 = createMockBar(40);

        smaIndicator.update(bar1);
        smaIndicator.update(bar2);
        smaIndicator.update(bar3);
        smaIndicator.update(bar4);

        verify(mockEventPublisher, times(4)).publishEvent(argThat(event ->
                event instanceof IndicatorEvent
        ));
        assertEquals(new Number("30.00"), smaIndicator.getValue());
        assertEquals(new Number("20.00"), smaIndicator.getValue(1));
    }

    @Test
    void testGetValueOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> smaIndicator.getValue(0));
        assertThrows(IndexOutOfBoundsException.class, () -> smaIndicator.getValue(-1));
    }

    @Test
    void testLongerPeriod() {
        SMA sma5 = new SMA("SMATest", 5, mockEventPublisher);
        for (int i = 1; i <= 5; i++) {
            sma5.update(createMockBar(i * 10));
        }

        verify(mockEventPublisher, times(5)).publishEvent(argThat(event ->
                event instanceof IndicatorEvent
        ));
        assertTrue(sma5.isReady());
        assertEquals(new Number("30.00"), sma5.getValue());
    }

    private Bar createMockBar(double closePrice) {
        Bar mockBar = Mockito.mock(Bar.class);
        when(mockBar.getClose()).thenReturn(new Number(BigDecimal.valueOf(closePrice).setScale(2, RoundingMode.HALF_UP)));
        return mockBar;
    }
}