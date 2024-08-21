package dev.jwtly10.core.model;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * A bar represents a single time period in a financial market. It is composed of four prices: open, high, low, and close.
 * It also includes the volume of the time period.
 *
 * @see Number [Number] for smart handling of prices
 */
public interface Bar {

    /**
     * @return the instrument of the bar (e.g. NAS100_USD, EUR_USD, etc.)
     */
    Instrument getInstrument();

    /**
     * Update the bar with a new tick
     * <p>
     * If the bar is not initialized, the open price and open time will be set to the tick values.
     * If the tick mid price is higher than the current high price, the high price will be updated.
     * If the tick mid price is lower than the current low price, the low price will be updated.
     * The close price will be updated with the tick mid price.
     * The close time will be set to the tick time.
     * The volume will be updated with the tick volume.
     * <p>
     *
     * @param tick the tick to update the bar with
     */
    void update(Tick tick);

    /**
     * @return the time period of the bar (e.g. 1 minute, 5 minutes, 1 day, etc.)
     */
    Duration getTimePeriod();

    /**
     * @return the date time of the bar open
     */
    ZonedDateTime getOpenTime();

    /**
     * @return the date time of the bar close
     */
    ZonedDateTime getCloseTime();

    /**
     * Set the close time of the bar
     *
     * @param closeTime the close time of the bar
     */
    void setCloseTime(ZonedDateTime closeTime);

    /**
     * @return the open price of the bar
     */
    Number getOpen();

    /**
     * @return the high price of the bar
     */

    Number getHigh();

    /**
     * @return the low price of the bar
     */
    Number getLow();

    /**
     * @return the close price of the bar
     */
    Number getClose();

    /**
     * @return the volume of the bar
     */
    Number getVolume();

    /**
     * @return True if the bar is bullish, false otherwise
     */
    default boolean isBearish() {
        return getClose().isLessThan(getOpen());
    }

    /**
     * @return True if the bar is bearish, false otherwise
     */
    default boolean isBullish() {
        return getClose().isGreaterThan(getOpen());
    }

    /**
     * @return a string representation of the bar
     */
    String toString();
}