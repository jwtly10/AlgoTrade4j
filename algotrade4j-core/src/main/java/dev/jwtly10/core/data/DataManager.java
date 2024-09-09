package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;

import java.time.ZonedDateTime;

/**
 * Interface for managing data operations and listeners.
 */
public interface DataManager {

    /**
     * Initialise the data manager with the current bar and the next bar close time.
     */
    void initialise(Bar currentBar, ZonedDateTime nextBarCloseTime);

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

    /**
     * Gets the bar series of a data manager
     *
     * @return the bar series
     */
    BarSeries getBarSeries();


    /**
     * Exposes where the strategy data will start from
     *
     * @return datetime where the data will start
     */
    ZonedDateTime getFrom();

    /**
     * Exposes where the strategy data will run until
     *
     * @return datetime where the data will end
     */
    ZonedDateTime getTo();

    /**
     * Exposes the number of ticks modelled in the strategy run
     *
     * @return the number of ticks modelled
     */
    int getTicksModeled();

    /**
     * During optimisation, alternate rules are needed regarding error handling.
     * This allows us to prevent stopping an entire data provider, just because one instance of a strategy failed.
     */
    void setIsOptimising(boolean value);
}