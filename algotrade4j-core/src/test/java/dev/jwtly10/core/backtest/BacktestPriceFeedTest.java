package dev.jwtly10.core.backtest;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.DefaultBar;
import dev.jwtly10.core.DefaultBarSeries;
import dev.jwtly10.core.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

class BacktestPriceFeedTest {
    private BacktestPriceFeed priceFeed;
    private final String symbol = "TEST";
    private final Number spread = new Number(0.02);

    @BeforeEach
    void setUp() {
        DefaultBarSeries barSeries = new DefaultBarSeries(10);
        LocalDateTime time = LocalDateTime.now();
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