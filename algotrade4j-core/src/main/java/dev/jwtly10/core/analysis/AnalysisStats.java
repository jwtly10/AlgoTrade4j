package dev.jwtly10.core.analysis;

public record AnalysisStats(double initialDeposit, double totalNetProfit, double grossProfit, double grossLoss, double profitFactor, double expectedPayoff, double maxDrawdown, int totalTrades, int totalLongTrades, int totalLongWinningTrades, double longWinPercentage, int totalShortTrades, int totalShortWinningTrades, double shortWinPercentage, double largestProfitableTrade, double largestLosingTrade, double averageProfitableTradeReturn, double averageLosingTradeReturn, int maxConsecutiveWins, int maxConsecutiveLosses, double maxConsecutiveProfit, double maxConsecutiveLoss, double averageConsecutiveWins, double averageConsecutiveLosses, double sharpeRatio, int ticksModelled) {
}
