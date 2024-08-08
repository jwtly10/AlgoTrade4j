package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;

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
     * Called when the data listener is stopped.
     */
    void onStop();
}