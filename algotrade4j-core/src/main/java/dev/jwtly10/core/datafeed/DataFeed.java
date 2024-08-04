package dev.jwtly10.core.datafeed;

import dev.jwtly10.core.BarDataListener;

public interface DataFeed {
    /**
     * Adds a listener to the data feed
     *
     * @param listener the listener to be added
     */
    void addBarDataListener(BarDataListener listener);

    /**
     * Starts the data feed
     */
    void start() throws DataFeedException;

    /**
     * Stops the data feed
     */
    void stop() throws DataFeedException;

    /**
     * Removes a listener from the data feed
     *
     * @param listener the listener to be removed
     */
    void removeBarDataListener(BarDataListener listener);
}