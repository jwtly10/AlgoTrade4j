package dev.jwtly10.core;

import dev.jwtly10.core.event.EventPublisher;

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
     * Called on each bar close (the bar is completed)
     * This method can be used to perform strategy logic based on the most recent bar of completed market data.
     *
     * @param bar The most recent bar of market data.
     */
    void onBarClose(Bar bar);

    /**
     * Called on each tick of market data.
     * This method can be used to perform additional processing on each tick.
     *
     * @param tick       The most recent tick of market data.
     * @param currentBar The current bar of market data (may be incomplete)
     */
    void onTick(Tick tick, Bar currentBar);

    /**
     * Called once after the strategy processing ends.
     * Use this method to perform any cleanup tasks or final calculations.
     */
    void onDeInit();
}