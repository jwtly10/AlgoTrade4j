package dev.jwtly10.core.analysis;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PerformanceAnalyser class is responsible for calculating the performance metrics of the trading strategy.
 * The data analysis captured, replicates what is seen in the MT4 Strategy Tester Analysis.
 * Bars in test
 * Ticks Modelled
 * Initial Deposit
 * Total net profit
 * Profit Factor
 * Absolute drawdown (not implemented)
 * Max Drawdown
 * Total Trades
 * Gross Profit
 * Gross Loss
 * Expected Payoff
 * Short positions (won %)
 * Long positions (won %)
 * Largest profitable trade
 * Largest losing trade
 * Average profitable trade
 * Average losing trade
 * Max consecutive wins
 * Max consecutive losses
 * Max consecutive profit
 * Max consecutive loss
 * Average consecutive wins
 * Average consecutive losses
 * <p>
 * Custom analysis fields:
 * Sharpe Ratio
 */
@Data
public class PerformanceAnalyser {
    // Equity History
    private final List<EquityPoint> equityHistory = new ArrayList<>();
    private double peakEquity = 0;

    // Stats
    private int ticksModelled = 0;
    private double sharpeRatio = 0;
    private double riskFreeRate = 0.02; // Assuming 2% annual risk-free rate

    // Balance stats
    private double initialDeposit = 0;
    private double totalNetProfit = 0;
    private double openTradeProfit = 0;
    private double profitFactor = 0;
    private double maxDrawdown = 0;
    private double grossProfit = 0;
    private double grossLoss = 0;
    private double expectedPayoff = 0;

    // Trade stats
    private int openTrades = 0;
    private int totalTradeInclOpen = 0;
    private int totalClosedLongTrades = 0;
    private int totalLongWinningTrades = 0;
    private double longWinPercentage = 0;
    private int totalClosedShortTrades = 0;
    private int totalShortWinningTrades = 0;
    private double shortWinPercentage = 0;

    // Trade return stats
    private double largestProfitableTrade = 0;
    private double largestLosingTrade = 0;
    private double averageProfitableTradeReturn = 0;
    private double averageLosingTradeReturn = 0;

    private int maxConsecutiveWins = 0;
    private int maxConsecutiveLosses = 0;
    private double maxConsecutiveProfit = 0;
    private double maxConsecutiveLoss = 0;
    private double averageConsecutiveWins = 0;
    private double averageConsecutiveLosses = 0;

    /*
     * Update the equity history on each tick
     * @param equity The current equity
     * @param timestamp The timestamp of the tick
     */
    public void updateOnTick(double equity) {
        ticksModelled++;
        updateMaxDrawdown(equity);
    }

    public void updateOnBar(double equity, ZonedDateTime timestamp) {
        equityHistory.add(new EquityPoint(equity, timestamp));
    }

    /*
     * Calculate the performance statistics of the trading strategy
     * WARNING: This method should only be called during backtests/optimisations
     * @param trades The trades executed by the strategy
     * @param initialBalance The initial balance of the strategy
     */
    public void calculateStatistics(Map<Integer, Trade> trades, double initialBalance) {
        this.initialDeposit = initialBalance;
        this.totalTradeInclOpen = trades.size();
        List<Trade> closedTrades = new ArrayList<>(trades.values()).stream()
                .filter(trade -> trade.getClosePrice() != Number.ZERO)
                .toList();

        List<Trade> openTrades = new ArrayList<>(trades.values()).stream()
                .filter(trade -> trade.getClosePrice() == Number.ZERO)
                .toList();

        calculateBalanceStats(closedTrades);
        calculateTradeStats(closedTrades);
        calculateTradeReturnStats(closedTrades, openTrades);
        calculateConsecutiveStats(closedTrades);
        calculateSharpeRatio(closedTrades);
    }

    /*
     * Calculate the performance statistics of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateBalanceStats(List<Trade> closedTrades) {
        this.grossProfit = closedTrades.stream()
                .map(Trade::getProfit)
                .filter(profit -> profit > 0)
                .reduce(0.0, Double::sum);

        this.grossLoss = closedTrades.stream()
                .map(Trade::getProfit)
                .filter(profit -> profit < 0)
                .reduce(0.0, Double::sum);

        this.totalNetProfit = this.grossProfit + this.grossLoss;

        this.profitFactor = this.grossLoss == 0 ? 0 :
                this.grossProfit / Math.abs(this.grossLoss);

        int totalTrades = closedTrades.size();
        this.expectedPayoff = totalTrades == 0 ? 0 :
                this.totalNetProfit / totalTrades;
    }

    /*
     * Calculate the trade statistics of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateTradeStats(List<Trade> closedTrades) {
        for (Trade trade : closedTrades) {
            if (trade.isLong()) {
                this.totalClosedLongTrades++;
                if (trade.getProfit() > 0) {
                    this.totalLongWinningTrades++;
                }
            } else {
                this.totalClosedShortTrades++;
                if (trade.getProfit() > 0) {
                    this.totalShortWinningTrades++;
                }
            }
        }

        this.longWinPercentage = this.totalClosedLongTrades == 0 ? 0 :
                (this.totalLongWinningTrades / (double) this.totalClosedLongTrades) * 100;

        this.shortWinPercentage = this.totalClosedShortTrades == 0 ? 0 :
                (this.totalShortWinningTrades / (double) this.totalClosedShortTrades) * 100;
    }

    /*
     * Calculate the trade return statistics of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateTradeReturnStats(List<Trade> closeTrades, List<Trade> openTrades) {
        this.openTradeProfit = openTrades.stream()
                .map(Trade::getProfit)
                .reduce(0.0, Double::sum);

        this.openTrades = openTrades.size();

        this.largestProfitableTrade = closeTrades.stream()
                .map(Trade::getProfit)
                .max(Double::compare)
                .orElse(0.0);

        this.largestLosingTrade = closeTrades.stream()
                .map(Trade::getProfit)
                .min(Double::compare)
                .orElse(0.0);

        double totalProfitableTrades = closeTrades.stream()
                .map(Trade::getProfit)
                .filter(profit -> profit > 0)
                .reduce(0.0, Double::sum);

        double totalLosingTrades = closeTrades.stream()
                .map(Trade::getProfit)
                .filter(profit -> profit < 0)
                .reduce(0.0, Double::sum);

        int profitableTradesCount = (int) closeTrades.stream()
                .filter(t -> t.getProfit() > 0)
                .count();

        int losingTradesCount = (int) closeTrades.stream()
                .filter(t -> t.getProfit() < 0)
                .count();

        this.averageProfitableTradeReturn = profitableTradesCount == 0 ? 0 :
                totalProfitableTrades / profitableTradesCount;

        this.averageLosingTradeReturn = losingTradesCount == 0 ? 0 :
                totalLosingTrades / losingTradesCount;
    }

    /*
     * Calculate the consecutive statistics of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateConsecutiveStats(List<Trade> closedTrades) {
        int consecutiveWins = 0;
        int consecutiveLosses = 0;
        double consecutiveProfit = 0;
        double consecutiveLoss = 0;
        int totalConsecutiveWins = 0;
        int totalConsecutiveLosses = 0;
        int winStreaks = 0;
        int lossStreaks = 0;

        for (Trade trade : closedTrades) {
            if (trade.getProfit() > 0) {
                consecutiveWins++;
                consecutiveProfit = consecutiveProfit + trade.getProfit();

                if (consecutiveLosses >= 1) {
                    totalConsecutiveLosses += consecutiveLosses;
                    lossStreaks++;
                }
                consecutiveLosses = 0;
                consecutiveLoss = 0;

                this.maxConsecutiveWins = Math.max(this.maxConsecutiveWins, consecutiveWins);
                this.maxConsecutiveProfit = Math.max(
                        this.maxConsecutiveProfit,
                        consecutiveProfit
                );
            } else {
                if (consecutiveWins >= 1) {
                    totalConsecutiveWins += consecutiveWins;
                    winStreaks++;
                }
                consecutiveLosses++;
                consecutiveLoss = consecutiveLoss + trade.getProfit();
                consecutiveWins = 0;
                consecutiveProfit = 0;

                this.maxConsecutiveLosses = Math.max(this.maxConsecutiveLosses, consecutiveLosses);
                this.maxConsecutiveLoss = Math.min(
                        this.maxConsecutiveLoss,
                        consecutiveLoss
                );
            }
        }

        // Handle the case where the last trades were part of a streak
        if (consecutiveWins >= 1) {
            totalConsecutiveWins += consecutiveWins;
            winStreaks++;
        } else if (consecutiveLosses >= 1) {
            totalConsecutiveLosses += consecutiveLosses;
            lossStreaks++;
        }

        this.averageConsecutiveWins = winStreaks == 0 ? 0 :
                (double) totalConsecutiveWins / winStreaks;

        this.averageConsecutiveLosses = lossStreaks == 0 ? 0 :
                (double) totalConsecutiveLosses / lossStreaks;
    }

    /*
     * Calculate the Sharpe ratio of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateSharpeRatio(List<Trade> closedTrades) {
        if (closedTrades.isEmpty()) {
            this.sharpeRatio = 0;
            return;
        }

        double totalReturn = closedTrades.stream()
                .mapToDouble(Trade::getProfit)
                .sum();

        double averageReturn = totalReturn / closedTrades.size();

        double sumSquaredDeviations = closedTrades.stream()
                .mapToDouble(trade -> {
                    double deviation = trade.getProfit() - averageReturn;
                    return deviation * deviation;
                })
                .sum();

        double standardDeviation = Math.sqrt(sumSquaredDeviations / closedTrades.size());

        if (standardDeviation == 0) {
            this.sharpeRatio = 0;
        } else {
            this.sharpeRatio = (averageReturn - this.riskFreeRate) / standardDeviation;
        }
    }

    /*
     * Get the equity history of the trading strategy
     * @return The equity history
     */
    public List<EquityPoint> getEquityHistory() {
        return new ArrayList<>(equityHistory);
    }

    /*
     * Update the max drawdown of the trading strategy
     * @param equity The current equity
     */
    private void updateMaxDrawdown(double equity) {
        if (equity > peakEquity) {
            peakEquity = equity;
        }
        double drawdown = ((peakEquity - equity) / peakEquity) * 100;

        if (drawdown > maxDrawdown) {
            maxDrawdown = drawdown;
        }
    }

    public record EquityPoint(double equity, ZonedDateTime timestamp) {
    }
}