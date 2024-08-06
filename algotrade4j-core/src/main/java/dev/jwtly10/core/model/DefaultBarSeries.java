package dev.jwtly10.core.model;

import java.util.LinkedList;
import java.util.List;

public class DefaultBarSeries implements BarSeries {
    private final String name;
    private final int maximumBarCount;
    private final LinkedList<Bar> bars;

    public DefaultBarSeries(String name, int maximumBarCount) {
        this.name = name;
        this.maximumBarCount = maximumBarCount;
        this.bars = new LinkedList<>();
    }

    public DefaultBarSeries(int maximumBarCount) {
        this("DefaultBarSeries", maximumBarCount);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getBarCount() {
        return bars.size();
    }

    @Override
    public Bar getBar(int index) {
        if (index < 0 || index >= bars.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + bars.size());
        }
        return bars.get(index);
    }

    @Override
    public Bar getLastBar() {
        if (bars.isEmpty()) {
            return null;
        }
        return bars.getLast();
    }

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

    @Override
    public int getMaximumBarCount() {
        return maximumBarCount;
    }

    @Override
    public void addBar(Bar bar) {
        bars.addLast(bar);
        if (bars.size() > maximumBarCount) {
            bars.removeFirst();
        }
    }
}