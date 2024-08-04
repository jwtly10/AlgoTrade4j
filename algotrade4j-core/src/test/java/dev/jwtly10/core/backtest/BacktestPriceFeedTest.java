package dev.jwtly10.core.backtest;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.Number;
import dev.jwtly10.core.defaults.DefaultBar;
import dev.jwtly10.core.defaults.DefaultBarSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BacktestPriceFeedTest {
    private final String symbol = "TEST";
    private final Number spread = new Number(0.02);
    private BacktestPriceFeed priceFeed;

    @BeforeEach
    void setUp() {
        DefaultBarSeries barSeries = new DefaultBarSeries(10);
        ZonedDateTime time = ZonedDateTime.now();
        Bar bar = DefaultBar.builder()
                .symbol(symbol)
                .timePeriod(Duration.ofMinutes(5))
                .dateTime(time)
                .open(new Number(100))
                .high(new Number(110))
                .low(new Number(90))
                .close(new Number(105))
                .volume(1000)
                .build();
        barSeries.addBar(bar);
        priceFeed = new BacktestPriceFeed(barSeries, spread);
    }

    @Test
    void testGetBid() {
        assertEquals(new Number(104.99), priceFeed.getBid(symbol));
    }

    @Test
    void testGetAsk() {
        assertEquals(new Number(105.01), priceFeed.getAsk(symbol));
    }

    @Test
    void testGetOpen() {
        assertEquals(new Number(100), priceFeed.getOpen(symbol));
    }

    @Test
    void testGetHigh() {
        assertEquals(new Number(110), priceFeed.getHigh(symbol));
    }

    @Test
    void testGetLow() {
        assertEquals(new Number(90), priceFeed.getLow(symbol));
    }

    @Test
    void testGetClose() {
        assertEquals(new Number(105), priceFeed.getClose(symbol));
    }

    @Test
    void testEmptyBarSeries() {
        DefaultBarSeries emptyBarSeries = new DefaultBarSeries(10);
        BacktestPriceFeed emptyPriceFeed = new BacktestPriceFeed(emptyBarSeries, spread);
        assertNull(emptyPriceFeed.getBid(symbol));
        assertNull(emptyPriceFeed.getAsk(symbol));
        assertNull(emptyPriceFeed.getOpen(symbol));
        assertNull(emptyPriceFeed.getHigh(symbol));
        assertNull(emptyPriceFeed.getLow(symbol));
        assertNull(emptyPriceFeed.getClose(symbol));
    }
}