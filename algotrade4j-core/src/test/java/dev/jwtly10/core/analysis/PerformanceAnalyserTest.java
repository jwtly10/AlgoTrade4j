package dev.jwtly10.core.analysis;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformanceAnalyserTest {

    private PerformanceAnalyser analyser;

    @BeforeEach
    void setUp() {
        analyser = new PerformanceAnalyser();
    }

    @Test
    void testUpdate() {
        ZonedDateTime now = ZonedDateTime.now();
        analyser.update(new Number(100), now);
        analyser.update(new Number(150), now.plusHours(1));

        List<PerformanceAnalyser.EquityPoint> history = analyser.getEquityHistory();
        assertEquals(2, history.size());
        assertEquals(new Number(100), history.get(0).equity());
        assertEquals(new Number(150), history.get(1).equity());
    }

    @Test
    void testUpdateMaxDrawdown() {
        analyser.update(new Number(100), ZonedDateTime.now());
        analyser.update(new Number(150), ZonedDateTime.now().plusHours(1));
        analyser.update(new Number(120), ZonedDateTime.now().plusHours(2));

        assertEquals(new Number(0.2), analyser.getMaxDrawdown());
        assertEquals(new Number(150), analyser.getPeakEquity());
    }

    @Test
    void testCalculateStatistics() {
        Map<Integer, Trade> trades = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();

        Trade longWinning = new Trade("AAPL", new Number(1), new Number(100), now, new Number(95), new Number(110), true);
        longWinning.setProfit(new Number(10));
        trades.put(longWinning.getId(), longWinning);

        Trade longLosing = new Trade("GOOGL", new Number(1), new Number(200), now, new Number(195), new Number(210), true);
        longLosing.setProfit(new Number(-5));
        trades.put(longLosing.getId(), longLosing);

        Trade shortWinning = new Trade("MSFT", new Number(1), new Number(150), now, new Number(155), new Number(140), false);
        shortWinning.setProfit(new Number(7));
        trades.put(shortWinning.getId(), shortWinning);

        Trade shortLosing = new Trade("AMZN", new Number(1), new Number(180), now, new Number(185), new Number(170), false);
        shortLosing.setProfit(new Number(-3));
        trades.put(shortLosing.getId(), shortLosing);

        analyser.calculateStatistics(trades);

        assertEquals(4, analyser.getTotalTrades());
        assertEquals(2, analyser.getTotalLongTrades());
        assertEquals(1, analyser.getTotalLongWinningTrades());
        assertEquals(new Number(2.5), analyser.getAverageLongTradeReturn());
        assertEquals(2, analyser.getTotalShortTrades());
        assertEquals(1, analyser.getTotalShortWinningTrades());
        assertEquals(new Number(2), analyser.getAverageShortTradeReturn());
    }

    @Test
    void testEmptyTrades() {
        analyser.calculateStatistics(new HashMap<>());

        assertEquals(0, analyser.getTotalTrades());
        assertEquals(0, analyser.getTotalLongTrades());
        assertEquals(0, analyser.getTotalLongWinningTrades());
        assertEquals(Number.ZERO, analyser.getAverageLongTradeReturn());
        assertEquals(0, analyser.getTotalShortTrades());
        assertEquals(0, analyser.getTotalShortWinningTrades());
        assertEquals(Number.ZERO, analyser.getAverageShortTradeReturn());
    }
}