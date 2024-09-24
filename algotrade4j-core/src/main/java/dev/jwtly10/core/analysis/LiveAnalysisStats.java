package dev.jwtly10.core.analysis;

/**
 * A simplified version of {@link AnalysisStats} used for live analysis data
 * This separate class is to improve performance data transfer
 */
public record LiveAnalysisStats(
        double balance,
        double equity,
        double openTradeProfit,
        double profitFactor,
        double sharpeRatio,
        int totalTrades,
        double winRate
) {
}