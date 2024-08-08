package dev.jwtly10.core.event;

import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.model.Number;
import lombok.Getter;

import java.util.List;

@Getter
public class AnalysisEvent extends BaseEvent {
    private final Number maxDrawdown;
    private final List<PerformanceAnalyser.EquityPoint> equityHistory;
    private final int totalTrades;
    private final int totalLongTrades;
    private final int totalLongWinningTrades;
    private final Number averageLongTradeReturn;
    private final int totalShortTrades;
    private final int totalShortWinningTrades;
    private final Number averageShortTradeReturn;
    private final int totalTicks;

    public AnalysisEvent(String strategyId, String symbol, PerformanceAnalyser performanceAnalyser) {
        super(strategyId, "ANALYSIS", symbol);
        this.maxDrawdown = performanceAnalyser.getMaxDrawdown();
        this.equityHistory = performanceAnalyser.getEquityHistory();
        this.totalTrades = performanceAnalyser.getTotalTrades();
        this.totalLongTrades = performanceAnalyser.getTotalLongTrades();
        this.totalLongWinningTrades = performanceAnalyser.getTotalLongWinningTrades();
        this.averageLongTradeReturn = performanceAnalyser.getAverageLongTradeReturn();
        this.totalShortTrades = performanceAnalyser.getTotalShortTrades();
        this.totalShortWinningTrades = performanceAnalyser.getTotalShortWinningTrades();
        this.averageShortTradeReturn = performanceAnalyser.getAverageShortTradeReturn();
        this.totalTicks = performanceAnalyser.getTotalTicks();
    }
}