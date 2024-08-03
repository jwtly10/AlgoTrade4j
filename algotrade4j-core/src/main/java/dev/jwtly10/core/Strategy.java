package dev.jwtly10.core;

import java.util.List;

/**
 * Represents a trading strategy in the AlgoTrade4j framework.
 */
public interface Strategy {
    /**
     * Called once before the strategy processing starts.
     * Use this method to initialize strategy-specific variables,
     * set up initial indicators, or perform any one-time setup tasks.
     *
     * @param series       The initial BarSeries available at strategy start.
     * @param indicators   List of indicators available to the strategy.
     * @param tradeManager The TradeManager instance for executing trades.
     */
    void onInit(BarSeries series, List<Indicator> indicators, TradeManager tradeManager);

    /**
     * Called on each new bar in the market data feed.
     * This is the main method where trading logic should be implemented.
     *
     * @param bar          The most recent bar of market data.
     * @param series       The updated BarSeries including the new bar.
     * @param indicators   List of indicators, updated with the new bar's data.
     * @param tradeManager The TradeManager instance for executing trades.
     */
    void onBar(Bar bar, BarSeries series, List<Indicator> indicators, TradeManager tradeManager);

    /**
     * Called on each tick of market data.
     * NOT IMPLEMENTED YET - DO NOT USE
     *
     * @throws UnsupportedOperationException if called before implementation.
     */
    default void onTick() {
        throw new UnsupportedOperationException("onTick is not implemented yet.");
    }

    /**
     * Called once after the strategy processing ends.
     * Use this method to perform any cleanup tasks or final calculations.
     */
    void onDeInit();
}