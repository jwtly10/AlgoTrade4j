package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;

import java.time.ZonedDateTime;

/**
 * Interface for listening to data events such as ticks and bar closures.
 * Mainly used for engines that facilitate trading strategies
 */
public interface DataListener {

    /**
     * Called when the data listener is initialised.
     */
    void initialise() throws Exception;

    /**
     * Get the data manager
     *
     * @return the data manager
     */
    DataManager getDataManager();

    /**
     * Called when a new tick is received.
     *
     * @param tick       the new tick data
     * @param currentBar the current bar data
     */
    void onTick(Tick tick, Bar currentBar);

    /**
     * Called when a bar is closed.
     *
     * @param bar the closed bar data
     */
    void onBarClose(Bar bar);

    /**
     * Called when the there is a new day
     */
    void onNewDay(ZonedDateTime newDay);

    /**
     * Called when the data listener is stopped.
     *
     * @param reason the reason for stopping the data listener
     */
    void onStop(String reason);

    /**
     * Called when a trade is closed
     */
    void onTradeClose(Trade trade);

    /**
     * The id of the data listener
     *
     * @return the strategy id of the data listener
     */
    String getStrategyId();

}