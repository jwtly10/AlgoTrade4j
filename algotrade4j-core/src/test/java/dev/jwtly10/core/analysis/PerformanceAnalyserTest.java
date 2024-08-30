package dev.jwtly10.core.analysis;

import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PerformanceAnalyserTest {

    private PerformanceAnalyser analyser;

    @BeforeEach
    void setUp() {
        analyser = new PerformanceAnalyser();
    }

    @Test
    void testPerformanceAnalysis() {
        Map<Integer, Trade> trades = new HashMap<>();
        Number initialBalance = new Number(10000);
        ZonedDateTime now = ZonedDateTime.now();

        trades.put(1, createTrade(1, new Number(1), now, new Number(10000), new Number(9900), new Number(10100), true, new Number(500), new Number(10050), now.plusHours(1)));
        trades.put(2, createTrade(2, new Number(1), now.plusHours(2), new Number(10100), new Number(10000), new Number(10200), false, new Number(300), new Number(10130), now.plusHours(3)));
        trades.put(3, createTrade(3, new Number(1), now.plusHours(4), new Number(10200), new Number(10100), new Number(10300), true, new Number(400), new Number(10240), now.plusHours(5)));
        trades.put(4, createTrade(4, new Number(1), now.plusHours(6), new Number(10300), new Number(10200), new Number(10400), false, new Number(-200), new Number(10320), now.plusHours(7)));
        trades.put(5, createTrade(5, new Number(1), now.plusHours(8), new Number(10400), new Number(10300), new Number(10500), true, new Number(600), new Number(10460), now.plusHours(9)));

        analyser.updateOnTick(10000);
        analyser.updateOnTick((10500));
        analyser.updateOnTick((10200));
        analyser.updateOnTick((10600));
        analyser.updateOnTick((10400));
        analyser.updateOnTick((11000));

        assertEquals(6, analyser.getTicksModelled());

        // Calculate statistics
        analyser.calculateStatistics(trades, initialBalance);

        // Assert balance stats
        assertEquals(new Number(1600), analyser.getTotalNetProfit());
        assertEquals(new Number(1800), analyser.getGrossProfit());
        assertEquals(new Number(-200), analyser.getGrossLoss());
        assertEquals(new Number(9), analyser.getProfitFactor());
        assertEquals(new Number(320), analyser.getExpectedPayoff());

        // Assert trade stats
        assertEquals(5, analyser.getTotalTrades());
        assertEquals(3, analyser.getTotalLongTrades());
        assertEquals(3, analyser.getTotalLongWinningTrades());
        assertEquals(new Number(100), analyser.getLongWinPercentage());
        assertEquals(2, analyser.getTotalShortTrades());
        assertEquals(1, analyser.getTotalShortWinningTrades());
        assertEquals(new Number(50), analyser.getShortWinPercentage());

        // Assert trade return stats
        assertEquals(new Number(600), analyser.getLargestProfitableTrade());
        assertEquals(new Number(-200), analyser.getLargestLosingTrade());
        assertEquals(new Number(450), analyser.getAverageProfitableTradeReturn());
        assertEquals(new Number(-200), analyser.getAverageLosingTradeReturn());

        // Assert consecutive stats
        assertEquals(3, analyser.getMaxConsecutiveWins());
        assertEquals(1, analyser.getMaxConsecutiveLosses());
        assertEquals(new Number(1200), analyser.getMaxConsecutiveProfit());
        assertEquals(new Number(-200), analyser.getMaxConsecutiveLoss());
        assertEquals(new Number(2), analyser.getAverageConsecutiveWins());
        assertEquals(new Number(1), analyser.getAverageConsecutiveLosses());

        assertEquals(2.85, analyser.getMaxDrawdown(), 0.01);

        // Assert Sharpe ratio
        assertTrue(analyser.getSharpeRatio().isGreaterThan(Number.ZERO));
    }

    @Test
    void testEmptyTradeList() {
        Map<Integer, Trade> trades = new HashMap<>();
        Number initialBalance = new Number(10000);
        analyser.calculateStatistics(trades, initialBalance);

        assertEquals(Number.ZERO, analyser.getTotalNetProfit());
        assertEquals(Number.ZERO, analyser.getGrossProfit());
        assertEquals(Number.ZERO, analyser.getGrossLoss());
        assertEquals(Number.ZERO, analyser.getProfitFactor());
        assertEquals(Number.ZERO, analyser.getExpectedPayoff());
        assertEquals(0, analyser.getTotalTrades());
        assertEquals(Number.ZERO, analyser.getSharpeRatio());
    }

    @Test
    void testAllWinningTrades() {
        Map<Integer, Trade> trades = new HashMap<>();
        Number initialBalance = new Number(10000);
        ZonedDateTime now = ZonedDateTime.now();

        trades.put(1, createTrade(1, new Number(1), now, new Number(100), new Number(90), new Number(110), true, new Number(100), new Number(110), now.plusHours(1)));
        trades.put(2, createTrade(2, new Number(1), now.plusHours(2), new Number(110), new Number(100), new Number(120), false, new Number(50), new Number(105), now.plusHours(3)));

        analyser.calculateStatistics(trades, initialBalance);

        assertEquals(new Number(150), analyser.getTotalNetProfit());
        assertEquals(new Number(150), analyser.getGrossProfit());
        assertEquals(Number.ZERO, analyser.getGrossLoss());
        assertTrue(analyser.getProfitFactor().equals(Number.ZERO) ||
                        analyser.getProfitFactor().isGreaterThan(new Number("1E10")),
                "Profit factor should be undefined (represented as zero) or a very large number");
        assertEquals(new Number(75), analyser.getExpectedPayoff());
        assertEquals(2, analyser.getTotalTrades());
        assertEquals(new Number(100), analyser.getLongWinPercentage());
        assertEquals(new Number(100), analyser.getShortWinPercentage());
    }

    @Test
    void testAllLosingTrades() {
        Map<Integer, Trade> trades = new HashMap<>();
        Number initialBalance = new Number(10000);
        ZonedDateTime now = ZonedDateTime.now();

        trades.put(1, createTrade(1, new Number(1), now, new Number(100), new Number(90), new Number(110), true, new Number(-50), new Number(95), now.plusHours(1)));
        trades.put(2, createTrade(2, new Number(1), now.plusHours(2), new Number(110), new Number(100), new Number(120), false, new Number(-30), new Number(113), now.plusHours(3)));

        analyser.calculateStatistics(trades, initialBalance);

        assertEquals(new Number(-80), analyser.getTotalNetProfit());
        assertEquals(Number.ZERO, analyser.getGrossProfit());
        assertEquals(new Number(-80), analyser.getGrossLoss());
        assertEquals(Number.ZERO, analyser.getProfitFactor());
        assertEquals(new Number(-40), analyser.getExpectedPayoff());
        assertEquals(2, analyser.getTotalTrades());
        assertEquals(Number.ZERO, analyser.getLongWinPercentage());
        assertEquals(Number.ZERO, analyser.getShortWinPercentage());
    }

    @Test
    void testConsecutiveWinsAndLosses() {
        Map<Integer, Trade> trades = new HashMap<>();
        Number initialBalance = new Number(10000);
        ZonedDateTime now = ZonedDateTime.now();

        trades.put(1, createTrade(1, new Number(1), now, new Number(100), new Number(90), new Number(110), true, new Number(100), new Number(110), now.plusHours(1)));
        trades.put(2, createTrade(2, new Number(1), now.plusHours(2), new Number(110), new Number(100), new Number(120), true, new Number(50), new Number(115), now.plusHours(3)));
        trades.put(3, createTrade(3, new Number(1), now.plusHours(4), new Number(115), new Number(105), new Number(125), false, new Number(-30), new Number(118), now.plusHours(5)));
        trades.put(4, createTrade(4, new Number(1), now.plusHours(6), new Number(118), new Number(108), new Number(128), false, new Number(-40), new Number(122), now.plusHours(7)));

        analyser.calculateStatistics(trades, initialBalance);

        assertEquals(2, analyser.getMaxConsecutiveWins());
        assertEquals(2, analyser.getMaxConsecutiveLosses());
        assertEquals(new Number(150), analyser.getMaxConsecutiveProfit());
        assertEquals(new Number(-70), analyser.getMaxConsecutiveLoss());
    }

    @Test
    void testDrawdown() {
        Number initialBalance = new Number(10000);
        ZonedDateTime now = ZonedDateTime.now();

        analyser.updateOnTick((10000));
        analyser.updateOnTick((10500));
        analyser.updateOnTick((10200));
        analyser.updateOnTick((9800));
        analyser.updateOnTick((10100));

        assertEquals(6.66, analyser.getMaxDrawdown(), 0.01);
    }

    @Test
    void testSharpeRatioWithConstantReturns() {
        Map<Integer, Trade> trades = new HashMap<>();
        Number initialBalance = new Number(10000);
        ZonedDateTime now = ZonedDateTime.now();

        for (int i = 1; i <= 10; i++) {
            trades.put(i, createTrade(i, new Number(1), now.plusHours(i), new Number(100 + i * 10), new Number(90 + i * 10), new Number(110 + i * 10), true, new Number(50), new Number(105 + i * 10), now.plusHours(i + 1)));
        }

        analyser.calculateStatistics(trades, initialBalance);

        assertEquals(Number.ZERO, analyser.getSharpeRatio());
    }

    private Trade createTrade(int id, Number quantity, ZonedDateTime openTime, Number entryPrice, Number stopLoss, Number takeProfit, boolean isLong, Number profit, Number closePrice, ZonedDateTime closeTime) {
        Trade trade = new Trade(id, Instrument.NAS100USD, quantity, openTime, entryPrice, stopLoss, takeProfit, isLong);
        trade.setProfit(profit.roundMoneyDown());
        trade.setClosePrice(closePrice);
        trade.setCloseTime(closeTime);
        return trade;
    }
}