package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;

/**
 * Interface for managing data operations and listeners.
 */
public interface DataManager {

    /**
     * Starts the data manager.
     */
    void start();

    /**
     * Stops the data manager.
     */
    void stop();

    /**
     * Adds a data listener to receive data events.
     *
     * @param listener the data listener to add
     */
    void addDataListener(DataListener listener);

    /**
     * Removes a data listener.
     *
     * @param listener the data listener to remove
     */
    void removeDataListener(DataListener listener);

    /**
     * Checks if the data manager is running.
     *
     * @return true if the data manager is running, false otherwise
     */
    boolean isRunning();

    /**
     * Gets the current bid price.
     *
     * @return the current bid price
     */
    Number getCurrentBid();

    /**
     * Gets the current ask price.
     *
     * @return the current ask price
     */
    Number getCurrentAsk();

    /**
     * Gets the current mid price.
     *
     * @return the current mid price
     */
    Number getCurrentMidPrice();

    /**
     * Gets the instrument associated with the data manager.
     *
     * @return the instrument
     */
    Instrument getInstrument();
}