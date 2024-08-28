package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;

import java.time.ZonedDateTime;

/**
 * Interface for listening to data events such as ticks and bar closures.
 */
public interface DataListener {

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
     */
    void onStop();

    /**
     * The id of the data listener
     *
     * @return the strategy id of the data listener
     */
    String getStrategyId();

}