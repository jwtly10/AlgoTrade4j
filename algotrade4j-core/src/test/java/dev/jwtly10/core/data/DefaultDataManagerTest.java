package dev.jwtly10.core.data;

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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class DefaultDataManagerTest {

    @Mock
    private DataProvider mockDataProvider;

    @Mock
    private BarSeries mockBarSeries;

    private DefaultDataManager dataManager;

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
        dataManager = new DefaultDataManager("BTCUSD", mockDataProvider, Duration.ofMinutes(1), mockBarSeries);
        dataManager.start();

        ZonedDateTime time = ZonedDateTime.parse("2023-01-01T00:00:00Z");
        DefaultTick tick1 = new DefaultTick("BTCUSD", new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), time);
        DefaultTick tick2 = new DefaultTick("BTCUSD", new Number("102"), new Number("102.5"), new Number("103"), new Number("15"), time.plusSeconds(30));
        DefaultTick tick3 = new DefaultTick("BTCUSD", new Number("104"), new Number("104.5"), new Number("105"), new Number("20"), time.plusMinutes(1));

        dataManager.onTick(tick1);
        dataManager.onTick(tick2);
        dataManager.onTick(tick3);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));
        assertEquals(new Number("20"), dataManager.getCurrentBar().getVolume());
        assertEquals(new Number("104.5"), dataManager.getCurrentBar().getOpen());
    }

    @Test
    void testMissingDailyData() {
        dataManager = new DefaultDataManager("BTCUSD", mockDataProvider, Duration.ofDays(1), mockBarSeries);
        dataManager.start();

        ZonedDateTime time1 = ZonedDateTime.parse("2022-01-02T00:00:00Z");
        ZonedDateTime time2 = ZonedDateTime.parse("2022-01-04T00:00:00Z");

        DefaultTick tick1 = new DefaultTick("BTCUSD", new Number("16419.7"), new Number("16472.85"), new Number("16526.0"), new Number("209249"), time1);
        DefaultTick tick2 = new DefaultTick("BTCUSD", new Number("16278.4"), new Number("16280.9"), new Number("16283.4"), new Number("396027"), time2);

        dataManager.onTick(tick1);
        dataManager.onTick(tick2);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));
        assertEquals(time2.toLocalDate(), dataManager.getCurrentBar().getOpenTime().toLocalDate());
        assertEquals(time2.plus(Duration.ofDays(1)), dataManager.getCurrentBar().getCloseTime(), "Current bar should close 1 day after the last tick");
        assertEquals(time2.plus(Duration.ofDays(1)), dataManager.getNextBarCloseTime(), "Next bar close time should be 1 day after the last tick");
    }


    @ParameterizedTest
    @MethodSource("provideTimeFrames")
    void testDifferentTimeFrames(Duration barDuration, ZonedDateTime[] tickTimes, int expectedBars) {
        dataManager = new DefaultDataManager("BTCUSD", mockDataProvider, barDuration, mockBarSeries);
        dataManager.start();

        for (ZonedDateTime time : tickTimes) {
            DefaultTick tick = new DefaultTick("BTCUSD", new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), time);
            dataManager.onTick(tick);
        }

        verify(mockBarSeries, times(expectedBars)).addBar(any(Bar.class));
    }

    @Test
    void testLargeTimeGap() {
        dataManager = new DefaultDataManager("BTCUSD", mockDataProvider, Duration.ofHours(1), mockBarSeries);
        dataManager.start();

        ZonedDateTime time1 = ZonedDateTime.parse("2023-01-01T00:00:00Z");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-01-02T00:00:00Z");

        DefaultTick tick1 = new DefaultTick("BTCUSD", new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), time1);
        DefaultTick tick2 = new DefaultTick("BTCUSD", new Number("102"), new Number("102.5"), new Number("103"), new Number("15"), time2);

        dataManager.onTick(tick1);
        dataManager.onTick(tick2);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));
        assertEquals(time2.toLocalDate(), dataManager.getCurrentBar().getOpenTime().toLocalDate());
        assertEquals(time2.plus(Duration.ofHours(1)), dataManager.getCurrentBar().getCloseTime(), "Current bar should close 1 hour after the last tick");
    }

    @Test
    void testWeekendGap() {
        dataManager = new DefaultDataManager("BTCUSD", mockDataProvider, Duration.ofDays(1), mockBarSeries);
        dataManager.start();

        ZonedDateTime fridayTime = ZonedDateTime.parse("2023-01-06T00:00:00Z");
        ZonedDateTime mondayTime = ZonedDateTime.parse("2023-01-09T00:00:00Z");

        DefaultTick fridayTick = new DefaultTick("BTCUSD", new Number("100"), new Number("100.5"), new Number("101"), new Number("10"), fridayTime);
        DefaultTick mondayTick = new DefaultTick("BTCUSD", new Number("102"), new Number("102.5"), new Number("103"), new Number("15"), mondayTime);

        dataManager.onTick(fridayTick);
        dataManager.onTick(mondayTick);

        verify(mockBarSeries, times(1)).addBar(any(Bar.class));

        Bar currentBar = dataManager.getCurrentBar();
        assertNotNull(currentBar, "Current bar should not be null");
        assertEquals(mondayTime.plus(Duration.ofDays(1)), currentBar.getCloseTime(), "Current bar should close 1 day after the last tick");
        assertEquals(mondayTime.toLocalDate(), currentBar.getOpenTime().toLocalDate(), "Current bar should open on Monday");
        assertEquals(mondayTime, currentBar.getOpenTime(), "Bar open time should match Monday's tick time");
    }
}