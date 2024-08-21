package dev.jwtly10.core.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of the BarSeries interface.
 */
public class DefaultBarSeries implements BarSeries {
    /**
     * The name of the bar series.
     */
    private final String name;

    /**
     * The maximum number of bars the series can hold.
     */
    private final int maximumBarCount;

    /**
     * The list of bars in the series.
     */
    private final LinkedList<Bar> bars;

    /**
     * Constructs a DefaultBarSeries with the specified name and maximum bar count.
     *
     * @param name            the name of the bar series
     * @param maximumBarCount the maximum number of bars the series can hold
     */
    public DefaultBarSeries(String name, int maximumBarCount) {
        this.name = name;
        this.maximumBarCount = maximumBarCount;
        this.bars = new LinkedList<>();
    }

    /**
     * Constructs a DefaultBarSeries with the default name and specified maximum bar count.
     *
     * @param maximumBarCount the maximum number of bars the series can hold
     */
    public DefaultBarSeries(int maximumBarCount) {
        this("DefaultBarSeries", maximumBarCount);
    }

    /**
     * Returns the name of the bar series.
     *
     * @return the name of the bar series
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the number of bars in the series.
     *
     * @return the number of bars in the series
     */
    @Override
    public int getBarCount() {
        return bars.size();
    }

    /**
     * Returns the bar at the specified index.
     *
     * @param index the index of the bar to return
     * @return the bar at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public Bar getBar(int index) {
        if (index < 0 || index >= bars.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + bars.size());
        }
        return bars.get(index);
    }

    /**
     * Returns the last bar in the series.
     *
     * @return the last bar in the series, or null if the series is empty
     */
    @Override
    public Bar getLastBar() {
        if (bars.isEmpty()) {
            return null;
        }
        return bars.getLast();
    }

    @Override
    public List<Bar> getBars() {
        return bars;
    }

    /**
     * Returns a new BarSeries containing the last n bars.
     *
     * @param n the number of bars to include in the new series
     * @return a new BarSeries containing the last n bars
     * @throws IllegalArgumentException if the number of bars is not positive
     */
    @Override
    public BarSeries getLastBars(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of bars must be positive");
        }
        int start = Math.max(0, bars.size() - n);
        List<Bar> lastBars = bars.subList(start, bars.size());
        DefaultBarSeries series = new DefaultBarSeries(n);
        series.bars.addAll(lastBars);
        return series;
    }

    /**
     * Returns the maximum number of bars the series can hold.
     *
     * @return the maximum number of bars the series can hold
     */
    @Override
    public int getMaximumBarCount() {
        return maximumBarCount;
    }

    /**
     * Adds a bar to the series. If the series exceeds the maximum bar count, the oldest bar is removed.
     *
     * @param bar the bar to add to the series
     */
    @Override
    public void addBar(Bar bar) {
        bars.addLast(bar);
        if (bars.size() > maximumBarCount) {
            bars.removeFirst();
        }
    }
}