package dev.jwtly10.core.model;

/**
 * A series of {@link Bar bars}.
 */
public interface BarSeries {
    /**
     * @return the name of the bar series
     */
    String getName();

    /**
     * @return the number of bars in the series
     */
    int getBarCount();

    /**
     * @return the bar at a given index
     */
    Bar getBar(int index);

    /**
     * @return the last bar in the series
     */
    Bar getLastBar();

    /**
     * @return the last 'n' bars in the series
     */
    BarSeries getLastBars(int n);

    /**
     * @return the maximum number of bars that the series can hold
     */
    int getMaximumBarCount();

    /**
     * Adds a new bar to the series
     */
    void addBar(Bar bar);

    /**
     * @return True if the series is empty, false otherwise
     */
    default boolean isEmpty() {
        return getBarCount() == 0;
    }

    /**
     * @return True if the series is full, false otherwise
     */
    default boolean isFull() {
        return getBarCount() == getMaximumBarCount();
    }
}