package dev.jwtly10.core.event;

import dev.jwtly10.core.analysis.AnalysisStats;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

import java.util.List;

@Getter
public class AnalysisEvent extends BaseEvent {
    private final List<PerformanceAnalyser.EquityPoint> equityHistory;
    private final AnalysisStats stats;

    public AnalysisEvent(String strategyId, Instrument instrument, PerformanceAnalyser performanceAnalyser) {
        super(strategyId, "ANALYSIS", instrument);
        this.equityHistory = performanceAnalyser.getEquityHistory();
        this.stats = new AnalysisStats(
                performanceAnalyser.getInitialDeposit(),
                performanceAnalyser.getTotalNetProfit(),
                performanceAnalyser.getGrossProfit(),
                performanceAnalyser.getGrossLoss(),
                performanceAnalyser.getProfitFactor(),
                performanceAnalyser.getExpectedPayoff(),
                performanceAnalyser.getMaxDrawdown(),
                performanceAnalyser.getTotalTradeInclOpen(),
                performanceAnalyser.getTotalClosedLongTrades(),
                performanceAnalyser.getTotalLongWinningTrades(),
                performanceAnalyser.getLongWinPercentage(),
                performanceAnalyser.getTotalClosedShortTrades(),
                performanceAnalyser.getTotalShortWinningTrades(),
                performanceAnalyser.getShortWinPercentage(),
                performanceAnalyser.getLargestProfitableTrade(),
                performanceAnalyser.getLargestLosingTrade(),
                performanceAnalyser.getAverageProfitableTradeReturn(),
                performanceAnalyser.getAverageLosingTradeReturn(),
                performanceAnalyser.getMaxConsecutiveWins(),
                performanceAnalyser.getMaxConsecutiveLosses(),
                performanceAnalyser.getMaxConsecutiveProfit(),
                performanceAnalyser.getMaxConsecutiveLoss(),
                performanceAnalyser.getAverageConsecutiveWins(),
                performanceAnalyser.getAverageConsecutiveLosses(),
                performanceAnalyser.getSharpeRatio(),
                performanceAnalyser.getTicksModelled()
        );
    }
}