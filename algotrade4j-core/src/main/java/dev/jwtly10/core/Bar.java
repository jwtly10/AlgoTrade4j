package dev.jwtly10.core;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A bar represents a single time period in a financial market. It is composed of four prices: open, high, low, and close.
 * It also includes the volume of the time period.
 *
 * @see Price [Price] for smart handling of prices
 */
public interface Bar {

    /**
     * @return the time period of the bar (e.g. 1 minute, 5 minutes, 1 day, etc.)
     */
    Duration getTimePeriod();

    /**
     * @return the date time of the bar
     */
    LocalDateTime getDateTime();

    /**
     * @return the open price of the bar
     */
    Price getOpen();

    /**
     * @return the high price of the bar
     */

    Price getHigh();

    /**
     * @return the low price of the bar
     */
    Price getLow();

    /**
     * @return the close price of the bar
     */
    Price getClose();

    /**
     * @return the volume of the bar
     */
    long getVolume();

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
}