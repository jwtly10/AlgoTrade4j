package dev.jwtly10.core.data;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static dev.jwtly10.core.model.Instrument.NAS100USD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultDataManagerTest {

    @Mock
    private DataProvider mockDataProvider;

    @Mock
    private BarSeries mockBarSeries;

    @Mock
    private EventPublisher mockEventPublisher;

    @Mock
    private DataListener mockDataListener;

    private DefaultDataManager dataManager;

    private String STRAT_ID = "TEST";

    private static Stream<Arguments> provideTimeFrames() {
        return Stream.of(
                Arguments.of(Duration.ofMinutes(1),
                        new ZonedDateTime[]{
                                ZonedDateTime.parse("2023-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2023-01-01T00:00:30Z"),
                                ZonedDateTime.parse("2023-01-01T00:01:00Z"),
                                ZonedDateTime.parse("2023-01-01T00:01:30Z")
                        }, 1),
                Arguments.of(Duration.ofMinutes(5),
                        new ZonedDateTime[]{
                                ZonedDateTime.parse("2023-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2023-01-01T00:04:59Z"),
                                ZonedDateTime.parse("2023-01-01T00:05:00Z"),
                                ZonedDateTime.parse("2023-01-01T00:09:59Z")
                        }, 1),
                Arguments.of(Duration.ofHours(1),
                        new ZonedDateTime[]{
                                ZonedDateTime.parse("2023-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2023-01-01T00:59:59Z"),
                                ZonedDateTime.parse("2023-01-01T01:00:00Z"),
                                ZonedDateTime.parse("2023-01-01T01:59:59Z")
                        }, 1)
        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNormalBarCreation() {
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofMinutes(1), mockBarSeries, mockEventPublisher);
        dataManager.addDataListener(mockDataListener);
        dataManager.start();

        ZonedDateTime time = ZonedDateTime.parse("2023-01-01T00:00:00Z");
        DefaultTick tick1 = new DefaultTick(NAS100USD, new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), time);
        DefaultTick tick2 = new DefaultTick(NAS100USD, new Number("102"), new Number("102.5"), new Number("103"), new Number("15"), time.plusSeconds(30));
        DefaultTick tick3 = new DefaultTick(NAS100USD, new Number("104"), new Number("104.5"), new Number("105"), new Number("20"), time.plusMinutes(1));

        dataManager.onTick(tick1);
        dataManager.onTick(tick2);
        dataManager.onTick(tick3);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));
        assertEquals(new Number("20"), dataManager.getCurrentBar().getVolume());
        assertEquals(new Number("104.5"), dataManager.getCurrentBar().getOpen());
    }

    @Test
    void testMissingDailyData() {
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofDays(1), mockBarSeries, mockEventPublisher);
        dataManager.addDataListener(mockDataListener);
        dataManager.start();

        ZonedDateTime time1 = ZonedDateTime.parse("2022-01-02T00:00:00Z");
        ZonedDateTime time2 = ZonedDateTime.parse("2022-01-04T00:00:00Z");

        DefaultTick tick1 = new DefaultTick(NAS100USD, new Number("16419.7"), new Number("16472.85"), new Number("16526.0"), new Number("209249"), time1);
        DefaultTick tick2 = new DefaultTick(NAS100USD, new Number("16278.4"), new Number("16280.9"), new Number("16283.4"), new Number("396027"), time2);

        dataManager.onTick(tick1);
        dataManager.onTick(tick2);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));
        assertEquals(time2.toLocalDate(), dataManager.getCurrentBar().getOpenTime().toLocalDate());
        assertEquals(time2.plus(Duration.ofDays(1).minusSeconds(1)), dataManager.getCurrentBar().getCloseTime(), "Current bar should close 1 day after the last tick");
        assertEquals(time2.plus(Duration.ofDays(1)), dataManager.getNextBarCloseTime(), "Next bar close time should be 1 day after the last tick");
    }


    @ParameterizedTest
    @MethodSource("provideTimeFrames")
    void testDifferentTimeFrames(Duration barDuration, ZonedDateTime[] tickTimes, int expectedBars) {
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, barDuration, mockBarSeries, mockEventPublisher);
        dataManager.addDataListener(mockDataListener);
        dataManager.start();

        for (ZonedDateTime time : tickTimes) {
            DefaultTick tick = new DefaultTick(NAS100USD, new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), time);
            dataManager.onTick(tick);
        }

        verify(mockBarSeries, times(expectedBars)).addBar(any(Bar.class));
    }

    @Test
    void testLargeTimeGap() {
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofHours(1), mockBarSeries, mockEventPublisher);
        dataManager.addDataListener(mockDataListener);
        dataManager.start();

        ZonedDateTime time1 = ZonedDateTime.parse("2023-01-01T00:00:00Z");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-01-02T00:00:00Z");

        DefaultTick tick1 = new DefaultTick(NAS100USD, new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), time1);
        DefaultTick tick2 = new DefaultTick(NAS100USD, new Number("102"), new Number("102.5"), new Number("103"), new Number("15"), time2);

        dataManager.onTick(tick1);
        dataManager.onTick(tick2);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));
        assertEquals(time2.toLocalDate(), dataManager.getCurrentBar().getOpenTime().toLocalDate());
        assertEquals(time2.plus(Duration.ofHours(1)).minusSeconds(1), dataManager.getCurrentBar().getCloseTime(), "Current bar should close 1 hour after the last tick");
    }

    @Test
    void testWeekendGap() {
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofDays(1), mockBarSeries, mockEventPublisher);
        dataManager.addDataListener(mockDataListener);
        dataManager.start();

        ZonedDateTime fridayTime = ZonedDateTime.parse("2023-01-06T00:00:00Z");
        ZonedDateTime mondayTime = ZonedDateTime.parse("2023-01-09T00:00:00Z");

        DefaultTick fridayTick = new DefaultTick(NAS100USD, new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), fridayTime);
        DefaultTick mondayTick = new DefaultTick(NAS100USD, new Number("102"), new Number("102.5"), new Number("103"), new Number("15"), mondayTime);

        dataManager.onTick(fridayTick);
        dataManager.onTick(mondayTick);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));

        Bar currentBar = dataManager.getCurrentBar();
        assertNotNull(currentBar, "Current bar should not be null");
        assertEquals(mondayTime.plus(Duration.ofDays(1)).minusSeconds(1), currentBar.getCloseTime(), "Current bar should close 1 day after the last tick");
        assertEquals(mondayTime.toLocalDate(), currentBar.getOpenTime().toLocalDate(), "Current bar should open on Monday");
        assertEquals(mondayTime, currentBar.getOpenTime(), "Bar open time should match Monday's tick time");
    }

    @Test
    void testDataProviderExceptionDuringStart() throws DataProviderException {
        doThrow(new DataProviderException("Test exception")).when(mockDataProvider).start();
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofDays(1), mockBarSeries, mockEventPublisher);
        dataManager.start();
        assertFalse(dataManager.isRunning());
        verify(mockEventPublisher).publishErrorEvent(eq(STRAT_ID), any(DataProviderException.class));
    }

    @Test
    void testStopWhenNotRunning() {
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofDays(1), mockBarSeries, mockEventPublisher);
        dataManager.stop();
        verify(mockDataProvider, never()).stop();
    }

    @Test
    void testTickExactlyAtBarCloseTime() {
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofMinutes(1), mockBarSeries, mockEventPublisher);
        dataManager.addDataListener(mockDataListener);
        dataManager.start();

        ZonedDateTime time = ZonedDateTime.parse("2023-01-01T00:00:00Z");
        DefaultTick tick1 = new DefaultTick(NAS100USD, new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), time);
        DefaultTick tick2 = new DefaultTick(NAS100USD, new Number("102"), new Number("102.5"), new Number("103"), new Number("15"), time.plusMinutes(1));

        dataManager.onTick(tick1);
        dataManager.onTick(tick2);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));
        assertEquals(new Number("15"), dataManager.getCurrentBar().getVolume());
    }

    @Test
    void testListenerNotifications() {
        DataListener mockListener = mock(DataListener.class);
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofDays(1), mockBarSeries, mockEventPublisher);
        dataManager.addDataListener(mockListener);
        dataManager.start();

        ZonedDateTime time = ZonedDateTime.parse("2023-01-01T00:00:00Z");
        DefaultTick tick = new DefaultTick(NAS100USD, new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), time);

        dataManager.onTick(tick);

        verify(mockListener).onTick(eq(tick), any(Bar.class));
        verify(mockListener).onNewDay(eq(tick.getDateTime()));

        dataManager.removeDataListener(mockListener);
        dataManager.onTick(tick);

        verifyNoMoreInteractions(mockListener);
    }

    @Test
    void testGetCurrentMidPrice() {
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofDays(1), mockBarSeries, mockEventPublisher);
        dataManager.start();
        dataManager.onTick(new DefaultTick(NAS100USD, new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), ZonedDateTime.now()));
        assertEquals(new Number("100.5"), dataManager.getCurrentMidPrice());

        // Reset to null
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofMinutes(1), mockBarSeries, mockEventPublisher);
        assertNull(dataManager.getCurrentMidPrice());
    }

    @Test
    void testMultipleDayScenario() {
        DataListener mockListener = mock(DataListener.class);
        dataManager = new DefaultDataManager(STRAT_ID, NAS100USD, mockDataProvider, Duration.ofDays(1), mockBarSeries, mockEventPublisher);
        dataManager.addDataListener(mockListener);
        dataManager.start();

        ZonedDateTime day1 = ZonedDateTime.parse("2023-01-01T00:00:00Z");
        ZonedDateTime day2 = ZonedDateTime.parse("2023-01-02T00:00:00Z");

        dataManager.onTick(new DefaultTick(NAS100USD, new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), day1));
        dataManager.onTick(new DefaultTick(NAS100USD, new Number("102"), new Number("102.5"), new Number("103"), new Number("15"), day2));

        verify(mockListener).onNewDay(day2.truncatedTo(ChronoUnit.DAYS));
    }
}