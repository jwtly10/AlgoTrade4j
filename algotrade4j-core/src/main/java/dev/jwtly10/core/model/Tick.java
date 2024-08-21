package dev.jwtly10.core.model;

import java.time.ZonedDateTime;

/**
 * Represents a market tick, which is a single price and volume data point at a specific time.
 */
public interface Tick {

    /**
     * Gets the instrument of the financial instrument.
     *
     * @return the instrument of the financial instrument
     */
    Instrument getInstrument();

    Number getBid();

    Number getAsk();

    Number getMid();

    /**
     * Gets the volume of the financial instrument traded at the time of the tick.
     *
     * @return the volume of the financial instrument
     */
    Number getVolume();

    /**
     * Gets the date and time of the tick.
     *
     * @return the date and time of the tick
     */
    ZonedDateTime getDateTime();

    void update(Tick tick);
}