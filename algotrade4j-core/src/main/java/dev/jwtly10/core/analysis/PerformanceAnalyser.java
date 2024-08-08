package dev.jwtly10.core.analysis;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PerformanceAnalyser {
    private final List<EquityPoint> equityHistory = new ArrayList<>();
    private Number maxDrawdown = Number.ZERO;
    private Number peakEquity = Number.ZERO;
    private int totalTrades = 0;
    private int totalLongTrades = 0;
    private int totalLongWinningTrades = 0;
    private int longTradeWinRate = 0;
    private Number averageLongTradeReturn = Number.ZERO;
    private int totalShortTrades = 0;
    private int totalShortWinningTrades = 0;
    private Number averageShortTradeReturn = Number.ZERO;
    private int totalTicks = 0;

    public void update(Number equity, ZonedDateTime timestamp) {
        equityHistory.add(new EquityPoint(equity, timestamp));
        updateMaxDrawdown(equity);
    }

    private void updateMaxDrawdown(Number equity) {
        if (equity.isGreaterThan(peakEquity)) {
            peakEquity = equity;
        }
        Number drawdown = peakEquity.subtract(equity).divide(peakEquity.getValue());
        if (drawdown.isGreaterThan(maxDrawdown)) {
            maxDrawdown = drawdown;
        }
    }

    public void calculateStatistics(Map<Integer, Trade> trades) {
        this.totalTrades = trades.size();
        this.totalLongTrades = 0;
        this.totalLongWinningTrades = 0;
        Number totalLongReturn = Number.ZERO;
        this.totalShortTrades = 0;
        this.totalShortWinningTrades = 0;
        Number totalShortReturn = Number.ZERO;

        for (Trade trade : trades.values()) {
            if (trade.isLong()) {
                totalLongTrades++;
                if (trade.getProfit().isGreaterThan(Number.ZERO)) totalLongWinningTrades++;
                totalLongReturn = totalLongReturn.add(trade.getProfit());
            } else {
                totalShortTrades++;
                if (trade.getProfit().isGreaterThan(Number.ZERO)) totalShortWinningTrades++;
                totalShortReturn = totalShortReturn.add(trade.getProfit());
            }
        }

        this.averageLongTradeReturn = totalLongTrades > 0 ?
                totalLongReturn.divide(totalLongTrades) : Number.ZERO;
        this.averageShortTradeReturn = totalShortTrades > 0 ?
                totalShortReturn.divide(totalShortTrades) : Number.ZERO;
    }

    public List<EquityPoint> getEquityHistory() {
        return new ArrayList<>(equityHistory);
    }

    public record EquityPoint(Number equity, ZonedDateTime timestamp) {
    }
}