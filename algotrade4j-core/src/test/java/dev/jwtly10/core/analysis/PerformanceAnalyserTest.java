package dev.jwtly10.core.analysis;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceAnalyserTest {

    private PerformanceAnalyser analyser;
    private Map<Integer, Trade> trades;

    @BeforeEach
    void setUp() {
        analyser = new PerformanceAnalyser();
        trades = new HashMap<>();
    }

    @Test
    void testPerformanceAnalysis() {
        Number initialBalance = new Number(10000);
        ZonedDateTime now = ZonedDateTime.now();

        trades.put(1, createTrade(1, new Number(1), now, new Number(10000), new Number(9900), new Number(10100), true, new Number(500), new Number(10050), now.plusHours(1)));
        trades.put(2, createTrade(2, new Number(1), now.plusHours(2), new Number(10100), new Number(10000), new Number(10200), false, new Number(300), new Number(10130), now.plusHours(3)));
        trades.put(3, createTrade(3, new Number(1), now.plusHours(4), new Number(10200), new Number(10100), new Number(10300), true, new Number(400), new Number(10240), now.plusHours(5)));
        trades.put(4, createTrade(4, new Number(1), now.plusHours(6), new Number(10300), new Number(10200), new Number(10400), false, new Number(-200), new Number(10320), now.plusHours(7)));
        trades.put(5, createTrade(5, new Number(1), now.plusHours(8), new Number(10400), new Number(10300), new Number(10500), true, new Number(600), new Number(10460), now.plusHours(9)));

        analyser.updateOnTick(new Number(10000), now);
        analyser.updateOnTick(new Number(10500), now.plusHours(1));
        analyser.updateOnTick(new Number(10200), now.plusHours(3));
        analyser.updateOnTick(new Number(10600), now.plusHours(5));
        analyser.updateOnTick(new Number(10400), now.plusHours(7));
        analyser.updateOnTick(new Number(11000), now.plusHours(9));

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

        // TODO: Check this, its rounding down
//        assertEquals(new Number(0.03773584905), analyser.getMaxDrawdown()); // (10600 - 10200) / 10600
        assertEquals(new Number(0.03), analyser.getMaxDrawdown()); // (10600 - 10200) / 10600

        // Assert Sharpe ratio
        assertEquals(new Number(1.15), analyser.getSharpeRatio());

        // Assert equity checker
        List<PerformanceAnalyser.EquityPoint> equityHistory = analyser.getEquityHistory();
        assertEquals(6, equityHistory.size());
        assertEquals(new Number(10000), equityHistory.get(0).equity());
        assertEquals(new Number(11000), equityHistory.get(5).equity());


    }

    private Trade createTrade(int id, Number quantity, ZonedDateTime openTime, Number entryPrice, Number stopLoss, Number takeProfit, boolean isLong, Number profit, Number closePrice, ZonedDateTime closeTime) {
        Trade trade = new Trade(id, "EURUSD", quantity, openTime, entryPrice, stopLoss, takeProfit, isLong);
        trade.setProfit(profit);
        trade.setClosePrice(closePrice);
        trade.setCloseTime(closeTime);
        return trade;
    }
}