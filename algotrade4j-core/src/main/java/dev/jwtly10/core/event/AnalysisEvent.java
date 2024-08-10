package dev.jwtly10.core.event;

import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.model.Number;
import lombok.Getter;

import java.util.List;

@Getter
public class AnalysisEvent extends BaseEvent {
    private final Number initialDeposit;
    private final Number totalNetProfit;
    private final Number grossProfit;
    private final Number grossLoss;
    private final Number profitFactor;
    private final Number expectedPayoff;
    private final Number maxDrawdown;
    private final List<PerformanceAnalyser.EquityPoint> equityHistory;
    private final int totalTrades;
    private final int totalLongTrades;
    private final int totalLongWinningTrades;
    private final Number longWinPercentage;
    private final int totalShortTrades;
    private final int totalShortWinningTrades;
    private final Number shortWinPercentage;
    private final Number largestProfitableTrade;
    private final Number largestLosingTrade;
    private final Number averageProfitableTradeReturn;
    private final Number averageLosingTradeReturn;
    private final int maxConsecutiveWins;
    private final int maxConsecutiveLosses;
    private final Number maxConsecutiveProfit;
    private final Number maxConsecutiveLoss;
    private final Number averageConsecutiveWins;
    private final Number averageConsecutiveLosses;
    private final Number sharpeRatio;
    private final int ticksModelled;

    public AnalysisEvent(String strategyId, String symbol, PerformanceAnalyser performanceAnalyser) {
        super(strategyId, "ANALYSIS", symbol);
        this.initialDeposit = performanceAnalyser.getInitialDeposit();
        this.totalNetProfit = performanceAnalyser.getTotalNetProfit();
        this.grossProfit = performanceAnalyser.getGrossProfit();
        this.grossLoss = performanceAnalyser.getGrossLoss();
        this.profitFactor = performanceAnalyser.getProfitFactor();
        this.expectedPayoff = performanceAnalyser.getExpectedPayoff();
        this.maxDrawdown = performanceAnalyser.getMaxDrawdown();
        this.equityHistory = performanceAnalyser.getEquityHistory();
        this.totalTrades = performanceAnalyser.getTotalTrades();
        this.totalLongTrades = performanceAnalyser.getTotalLongTrades();
        this.totalLongWinningTrades = performanceAnalyser.getTotalLongWinningTrades();
        this.longWinPercentage = performanceAnalyser.getLongWinPercentage();
        this.totalShortTrades = performanceAnalyser.getTotalShortTrades();
        this.totalShortWinningTrades = performanceAnalyser.getTotalShortWinningTrades();
        this.shortWinPercentage = performanceAnalyser.getShortWinPercentage();
        this.largestProfitableTrade = performanceAnalyser.getLargestProfitableTrade();
        this.largestLosingTrade = performanceAnalyser.getLargestLosingTrade();
        this.averageProfitableTradeReturn = performanceAnalyser.getAverageProfitableTradeReturn();
        this.averageLosingTradeReturn = performanceAnalyser.getAverageLosingTradeReturn();
        this.maxConsecutiveWins = performanceAnalyser.getMaxConsecutiveWins();
        this.maxConsecutiveLosses = performanceAnalyser.getMaxConsecutiveLosses();
        this.maxConsecutiveProfit = performanceAnalyser.getMaxConsecutiveProfit();
        this.maxConsecutiveLoss = performanceAnalyser.getMaxConsecutiveLoss();
        this.averageConsecutiveWins = performanceAnalyser.getAverageConsecutiveWins();
        this.averageConsecutiveLosses = performanceAnalyser.getAverageConsecutiveLosses();
        this.sharpeRatio = performanceAnalyser.getSharpeRatio();
        this.ticksModelled = performanceAnalyser.getTicksModelled();
    }
}