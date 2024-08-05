package dev.jwtly10.core;

import java.time.ZonedDateTime;

/**
 * Represents a market tick, which is a single price and volume data point at a specific time.
 */
public interface Tick {

    /**
     * Gets the symbol of the financial instrument.
     *
     * @return the symbol of the financial instrument
     */
    String getSymbol();

    /**
     * Gets the price of the financial instrument at the time of the tick.
     *
     * @return the price of the financial instrument
     */
    Number getPrice();

    /**
     * Gets the volume of the financial instrument traded at the time of the tick.
     *
     * @return the volume of the financial instrument
     */
    long getVolume();

    /**
     * Gets the date and time of the tick.
     *
     * @return the date and time of the tick
     */
    ZonedDateTime getDateTime();
}