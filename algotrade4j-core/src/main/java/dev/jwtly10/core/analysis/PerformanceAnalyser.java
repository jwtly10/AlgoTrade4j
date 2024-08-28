package dev.jwtly10.core.analysis;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private Number peakEquity = Number.ZERO;

    // Stats
    private int ticksModelled = 0;
    private Number sharpeRatio = Number.ZERO;
    private Number riskFreeRate = new Number(0.02); // Assuming 2% annual risk-free rate

    // Balance stats
    private Number initialDeposit = Number.ZERO;
    private Number totalNetProfit = Number.ZERO;
    private Number profitFactor = Number.ZERO;
    private Number maxDrawdown = Number.ZERO;
    private Number grossProfit = Number.ZERO;
    private Number grossLoss = Number.ZERO;
    private Number expectedPayoff = Number.ZERO;

    // Trade stats
    private int totalTrades = 0;
    private int totalLongTrades = 0;
    private int totalLongWinningTrades = 0;
    private Number longWinPercentage = Number.ZERO;
    private int totalShortTrades = 0;
    private int totalShortWinningTrades = 0;
    private Number shortWinPercentage = Number.ZERO;

    // Trade return stats
    private Number largestProfitableTrade = Number.ZERO;
    private Number largestLosingTrade = Number.ZERO;
    private Number averageProfitableTradeReturn = Number.ZERO;
    private Number averageLosingTradeReturn = Number.ZERO;

    private int maxConsecutiveWins = 0;
    private int maxConsecutiveLosses = 0;
    private Number maxConsecutiveProfit = Number.ZERO;
    private Number maxConsecutiveLoss = Number.ZERO;
    private Number averageConsecutiveWins = Number.ZERO;
    private Number averageConsecutiveLosses = Number.ZERO;

    /*
     * Update the equity history on each tick
     * @param equity The current equity
     * @param timestamp The timestamp of the tick
     */
    public void updateOnTick(Number equity, ZonedDateTime timestamp) {
        ticksModelled++;
        equityHistory.add(new EquityPoint(equity, timestamp));
        updateMaxDrawdown(equity);
    }

    /*
     * Calculate the performance statistics of the trading strategy
     * WARNING: This method should only be called during backtests/optimisations
     * @param trades The trades executed by the strategy
     * @param initialBalance The initial balance of the strategy
     */
    public void calculateStatistics(Map<Integer, Trade> trades, Number initialBalance) {
        this.initialDeposit = initialBalance;
        this.totalTrades = trades.size();
        List<Trade> tradeList = new ArrayList<>(trades.values());

        calculateBalanceStats(tradeList);
        calculateTradeStats(tradeList);
        calculateTradeReturnStats(tradeList);
        calculateConsecutiveStats(tradeList);
        calculateSharpeRatio(tradeList);
    }


    /*
     * Calculate the performance statistics of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateBalanceStats(List<Trade> trades) {
        this.grossProfit = trades.stream()
                .map(Trade::getProfit)
                .filter(profit -> profit.isGreaterThan(Number.ZERO))
                .reduce(Number.ZERO, Number::add);

        this.grossLoss = trades.stream()
                .map(Trade::getProfit)
                .filter(profit -> profit.isLessThan(Number.ZERO))
                .reduce(Number.ZERO, Number::add);

        this.totalNetProfit = this.grossProfit.add(this.grossLoss);

        this.profitFactor = this.grossLoss == Number.ZERO ? Number.ZERO :
                this.grossProfit.divide(this.grossLoss.abs().getValue());

        this.expectedPayoff = this.totalTrades == 0 ? Number.ZERO :
                this.totalNetProfit.divide(this.totalTrades);
    }

    /*
     * Calculate the trade statistics of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateTradeStats(List<Trade> trades) {
        for (Trade trade : trades) {
            if (trade.isLong()) {
                this.totalLongTrades++;
                if (trade.getProfit().isGreaterThan(Number.ZERO)) {
                    this.totalLongWinningTrades++;
                }
            } else {
                this.totalShortTrades++;
                if (trade.getProfit().isGreaterThan(Number.ZERO)) {
                    this.totalShortWinningTrades++;
                }
            }
        }

        this.longWinPercentage = this.totalLongTrades == 0 ? Number.ZERO :
                new Number(this.totalLongWinningTrades).divide(this.totalLongTrades).multiply(new BigDecimal(100)).setScale(2, RoundingMode.DOWN);

        this.shortWinPercentage = this.totalShortTrades == 0 ? Number.ZERO :
                new Number(this.totalShortWinningTrades).divide(this.totalShortTrades).multiply(new BigDecimal(100)).setScale(2, RoundingMode.DOWN);
    }

    /*
     * Calculate the trade return statistics of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateTradeReturnStats(List<Trade> trades) {
        this.largestProfitableTrade = trades.stream()
                .map(Trade::getProfit)
                .max(Number::compareTo)
                .orElse(Number.ZERO);

        this.largestLosingTrade = trades.stream()
                .map(Trade::getProfit)
                .min(Number::compareTo)
                .orElse(Number.ZERO);

        Number totalProfitableTrades = trades.stream()
                .map(Trade::getProfit)
                .filter(profit -> profit.isGreaterThan(Number.ZERO))
                .reduce(Number.ZERO, Number::add);

        Number totalLosingTrades = trades.stream()
                .map(Trade::getProfit)
                .filter(profit -> profit.isLessThan(Number.ZERO))
                .reduce(Number.ZERO, Number::add);

        int profitableTradesCount = (int) trades.stream()
                .filter(t -> t.getProfit().isGreaterThan(Number.ZERO))
                .count();

        int losingTradesCount = (int) trades.stream()
                .filter(t -> t.getProfit().isLessThan(Number.ZERO))
                .count();

        this.averageProfitableTradeReturn = profitableTradesCount == 0 ? Number.ZERO :
                totalProfitableTrades.divide(profitableTradesCount);

        this.averageLosingTradeReturn = losingTradesCount == 0 ? Number.ZERO :
                totalLosingTrades.divide(losingTradesCount);
    }

    /*
     * Calculate the consecutive statistics of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateConsecutiveStats(List<Trade> trades) {
        int consecutiveWins = 0;
        int consecutiveLosses = 0;
        Number consecutiveProfit = Number.ZERO;
        Number consecutiveLoss = Number.ZERO;
        int totalConsecutiveWins = 0;
        int totalConsecutiveLosses = 0;
        int winStreaks = 0;
        int lossStreaks = 0;

        for (Trade trade : trades) {
            if (trade.getProfit().isGreaterThan(Number.ZERO)) {
                consecutiveWins++;
                consecutiveProfit = consecutiveProfit.add(trade.getProfit());

                if (consecutiveLosses >= 1) {
                    totalConsecutiveLosses += consecutiveLosses;
                    lossStreaks++;
                }
                consecutiveLosses = 0;
                consecutiveLoss = Number.ZERO;

                this.maxConsecutiveWins = Math.max(this.maxConsecutiveWins, consecutiveWins);
                this.maxConsecutiveProfit = new Number(Math.max(
                        this.maxConsecutiveProfit.getValue().doubleValue(),
                        consecutiveProfit.getValue().doubleValue()
                ));
            } else {
                if (consecutiveWins >= 1) {
                    totalConsecutiveWins += consecutiveWins;
                    winStreaks++;
                }
                consecutiveLosses++;
                consecutiveLoss = consecutiveLoss.add(trade.getProfit());
                consecutiveWins = 0;
                consecutiveProfit = Number.ZERO;

                this.maxConsecutiveLosses = Math.max(this.maxConsecutiveLosses, consecutiveLosses);
                this.maxConsecutiveLoss = new Number(Math.min(
                        this.maxConsecutiveLoss.getValue().doubleValue(),
                        consecutiveLoss.getValue().doubleValue()
                ));
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

        this.averageConsecutiveWins = winStreaks == 0 ? Number.ZERO :
                new Number(totalConsecutiveWins).divide(winStreaks);

        this.averageConsecutiveLosses = lossStreaks == 0 ? Number.ZERO :
                new Number(totalConsecutiveLosses).divide(lossStreaks);
    }

    /*
     * Calculate the Sharpe ratio of the trading strategy
     * @param trades The trades executed by the strategy
     */
    private void calculateSharpeRatio(List<Trade> trades) {
        if (trades.isEmpty()) {
            this.sharpeRatio = Number.ZERO;
            return;
        }

        Number totalReturn = trades.stream()
                .map(Trade::getProfit)
                .reduce(Number.ZERO, Number::add);

        Number averageReturn = totalReturn.divide(trades.size());

        Number sumSquaredDeviations = trades.stream()
                .map(trade -> {
                    Number deviation = trade.getProfit().subtract(averageReturn);
                    return deviation.multiply(deviation.getValue());
                })
                .reduce(Number.ZERO, Number::add);

        Number standardDeviation = new Number(Math.sqrt(sumSquaredDeviations.divide(trades.size()).getValue().doubleValue()));

        if (standardDeviation.equals(Number.ZERO)) {
            this.sharpeRatio = Number.ZERO;
        } else {
            this.sharpeRatio = averageReturn.subtract(this.riskFreeRate).divide(standardDeviation.getValue());
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
    private void updateMaxDrawdown(Number equity) {
        if (equity.isGreaterThan(peakEquity)) {
            peakEquity = equity;
        }
        Number drawdown = peakEquity.subtract(equity).divide(peakEquity.getValue()).multiply(new Number(100));
        if (drawdown.isGreaterThan(maxDrawdown)) {
            maxDrawdown = drawdown.roundMoneyDown();
        }
    }

    public record EquityPoint(Number equity, ZonedDateTime timestamp) {
    }
}