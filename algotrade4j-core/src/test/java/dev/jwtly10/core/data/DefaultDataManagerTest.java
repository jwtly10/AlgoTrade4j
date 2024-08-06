package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

class DefaultDataManagerTest {

    private final Duration barDuration = Duration.ofMinutes(5);
    @Mock
    private DataProvider dataProvider;
    @Mock
    private BarSeries barSeries;
    @Mock
    private DataListener dataListener;
    private DefaultDataManager dataManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataManager = new DefaultDataManager("TEST", dataProvider, barDuration, barSeries);
        dataManager.addDataListener(dataListener);
    }

    @Test
    void testOnTickProcessing() {
        ZonedDateTime startTime = ZonedDateTime.of(2022, 4, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        Tick tick1 = createTick(startTime.plusSeconds(5), new Number("100"), new Number("101"));
        Tick tick2 = createTick(startTime.plusMinutes(2), new Number("102"), new Number("103"));
        Tick tick3 = createTick(startTime.plusMinutes(5).plusSeconds(1), new Number("104"), new Number("105"));

        dataManager.start();
        dataManager.onTick(tick1);
        dataManager.onTick(tick2);
        dataManager.onTick(tick3);

        verify(dataListener, times(3)).onTick(any(Tick.class), any(Bar.class));
        verify(barSeries, times(1)).addBar(any(Bar.class));
        // 2 Ticks within the 5 minute, the second one was beyond
        verify(dataListener, times(1)).onBarClose(any(Bar.class));
    }

    @Test
    void testBarCreationAndClosing() {
        ZonedDateTime startTime = ZonedDateTime.of(2022, 4, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        Tick tick1 = createTick(startTime, new Number("100"), new Number("101"));
        Tick tick2 = createTick(startTime.plusMinutes(5), new Number("102"), new Number("103"));
        Tick tick3 = createTick(startTime.plusMinutes(10), new Number("104"), new Number("105"));

        dataManager.start();
        dataManager.onTick(tick1);
        dataManager.onTick(tick2);
        dataManager.onTick(tick3);

        verify(barSeries, times(2)).addBar(any(Bar.class));
        verify(dataListener, times(2)).onBarClose(any(Bar.class));
    }

    @Test
    void testStopDataManager() {
        dataManager.start();

        dataManager.onStop();

        verify(dataListener, times(1)).onStop();
    }

    private Tick createTick(ZonedDateTime dateTime, Number bid, Number ask) {
        Tick tick = mock(Tick.class);
        when(tick.getDateTime()).thenReturn(dateTime);
        when(tick.getBid()).thenReturn(bid);
        when(tick.getAsk()).thenReturn(ask);
        when(tick.getMid()).thenReturn(bid.add(ask).divide(2));
        when(tick.getSymbol()).thenReturn("TEST");
        when(tick.getVolume()).thenReturn(new Number(1000));
        return tick;
    }
}