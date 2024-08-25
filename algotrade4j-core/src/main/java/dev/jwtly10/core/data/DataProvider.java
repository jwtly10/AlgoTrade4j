package dev.jwtly10.core.data;

import dev.jwtly10.core.exception.DataProviderException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interface for providing data and managing data provider listeners.
 */
public interface DataProvider {

    /**
     * Gets the date-time formatter used by the data provider.
     *
     * @return the date-time formatter
     */
    DateTimeFormatter getDateTimeFormatter();

    /**
     * Starts the data provider.
     *
     * @throws DataProviderException if an error occurs while starting the data provider
     */
    void start() throws DataProviderException;

    /**
     * Stops the data provider.
     */
    void stop();

    /**
     * Checks if the data provider is running.
     *
     * @return true if the data provider is running, false otherwise
     */
    boolean isRunning();

    /**
     * Adds a data provider listener to receive data events.
     *
     * @param listener the data provider listener to add
     */
    void addDataProviderListener(DataProviderListener listener);

    /**
     * Sets the data speed of the data provider.
     *
     * @param dataSpeed the data speed to set
     */
    void setDataSpeed(DataSpeed dataSpeed);

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
}