package dev.jwtly10.core.indicators;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.IndicatorEvent;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class iSMATest {

    private iSMA iSmaIndicator;
    private EventPublisher mockEventPublisher;


    @BeforeEach
    void setUp() {
        mockEventPublisher = mock(EventPublisher.class);
        iSmaIndicator = new iSMA(3);
        iSmaIndicator.setEventPublisher(mockEventPublisher);
        iSmaIndicator.setStrategyId("TESTING");
    }

    @Test
    void testInitialState() {
        assertEquals("SMA 3", iSmaIndicator.getName());
        assertEquals(3, iSmaIndicator.getRequiredPeriods());
        assertFalse(iSmaIndicator.isReady());
        assertEquals(0, iSmaIndicator.getValue());
    }

    @Test
    void testUpdate() {
        Bar bar1 = createMockBar(10);
        Bar bar2 = createMockBar(20);
        Bar bar3 = createMockBar(30);

        iSmaIndicator.update(bar1);

        assertFalse(iSmaIndicator.isReady());
        assertEquals(0, iSmaIndicator.getValue());

        iSmaIndicator.update(bar2);
        assertFalse(iSmaIndicator.isReady());
        assertEquals(0, iSmaIndicator.getValue());

        iSmaIndicator.update(bar3);
        assertTrue(iSmaIndicator.isReady());
        assertEquals(20.00, iSmaIndicator.getValue());

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

        iSmaIndicator.update(bar1);
        iSmaIndicator.update(bar2);
        iSmaIndicator.update(bar3);
        iSmaIndicator.update(bar4);

        verify(mockEventPublisher, times(4)).publishEvent(argThat(event ->
                event instanceof IndicatorEvent
        ));
        assertEquals(30.00, iSmaIndicator.getValue());
        assertEquals(20.00, iSmaIndicator.getValue(1));
    }

    @Test
    void testGetValueOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> iSmaIndicator.getValue(0));
        assertThrows(IndexOutOfBoundsException.class, () -> iSmaIndicator.getValue(-1));
    }

    @Test
    void testLongerPeriod() {
        iSMA iSma5 = new iSMA(5);
        iSma5.setEventPublisher(mockEventPublisher);
        for (int i = 1; i <= 5; i++) {
            iSma5.update(createMockBar(i * 10));
        }

        verify(mockEventPublisher, times(5)).publishEvent(argThat(event ->
                event instanceof IndicatorEvent
        ));
        assertTrue(iSma5.isReady());
        assertEquals(30.00, iSma5.getValue());
    }

    private Bar createMockBar(double closePrice) {
        Bar mockBar = Mockito.mock(Bar.class);
        when(mockBar.getClose()).thenReturn(new Number(BigDecimal.valueOf(closePrice).setScale(2, RoundingMode.HALF_UP)));
        return mockBar;
    }
}