package dev.jwtly10.core.event;

import com.fasterxml.jackson.databind.SerializationFeature;
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
                performanceAnalyser.getTotalTrades(),
                performanceAnalyser.getTotalLongTrades(),
                performanceAnalyser.getTotalLongWinningTrades(),
                performanceAnalyser.getLongWinPercentage(),
                performanceAnalyser.getTotalShortTrades(),
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

    // TODO: Remove (only for debug purposes)
    public String pretty() {
        try {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            return objectMapper.writeValueAsString(this.stats);
        } catch (Exception e) {
            return "Error generating JSON: " + e.getMessage();
        }
    }
}