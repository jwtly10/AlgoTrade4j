package dev.jwtly10.core.analysis;

import dev.jwtly10.core.model.Number;

public record AnalysisStats(Number initialDeposit, Number totalNetProfit, Number grossProfit, Number grossLoss, Number profitFactor, Number expectedPayoff, Number maxDrawdown, int totalTrades, int totalLongTrades, int totalLongWinningTrades, Number longWinPercentage, int totalShortTrades, int totalShortWinningTrades, Number shortWinPercentage, Number largestProfitableTrade, Number largestLosingTrade, Number averageProfitableTradeReturn, Number averageLosingTradeReturn, int maxConsecutiveWins,
                            int maxConsecutiveLosses, Number maxConsecutiveProfit, Number maxConsecutiveLoss, Number averageConsecutiveWins, Number averageConsecutiveLosses, Number sharpeRatio, int ticksModelled) {
}