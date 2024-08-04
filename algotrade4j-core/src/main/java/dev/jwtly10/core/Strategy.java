package dev.jwtly10.core;

import dev.jwtly10.core.event.EventPublisher;

import java.util.List;

/**
 * Represents a trading strategy in the AlgoTrade4j framework.
 */
public interface Strategy {

    /**
     * Called once before the strategy processing starts.
     * Abstracts away the initialisation of the strategy, and required dependencies.
     *
     * @param series         The initial BarSeries available at strategy start.
     * @param priceFeed      The PriceFeed instance for retrieving market data.
     * @param tradeManager   The TradeManager instance for executing trades.
     * @param eventPublisher The EventPublisher instance for publishing events.
     */
    void onInit(BarSeries series, PriceFeed priceFeed, TradeManager tradeManager, EventPublisher eventPublisher);

    /**
     * Called once after the strategy processing starts.
     * Use this method to perform any custom initialisation logic.
     */
    void onStart();

    /**
     * Returns a unique identifier for the strategy. TODO: Should this be unique? I guess callers can handle that - adding some unique key in the case of optimisation etc where multiple runs are happening TBC.
     *
     * @return The unique identifier for the strategy.
     */
    String getStrategyId();

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