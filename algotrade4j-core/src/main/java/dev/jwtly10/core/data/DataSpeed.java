package dev.jwtly10.core.data;

/**
 * Enum representing different data speeds with corresponding delay in milliseconds.
 * This is used for configuring the speed of the data feed during backtesting.
 * In a live system this will be controlled by the broker of the data feed
 */
public enum DataSpeed {
    SLOW(3000),    // 3 seconds per bar
    NORMAL(1000),  // 1 second per bar
    FAST(100),     // 100 milliseconds per bar
    VERY_FAST(10), // 10 milliseconds per bar
    INSTANT(0);    // No delay

    /**
     * The delay in milliseconds for the data speed.
     */
    public final long delayMillis;

    /**
     * Constructor for DataSpeed enum.
     *
     * @param delayMillis the delay in milliseconds
     */
    DataSpeed(long delayMillis) {
        this.delayMillis = delayMillis;
    }
}