package dev.jwtly10.core.indicators;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.Indicator;
import dev.jwtly10.core.Number;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a Simple Moving Average (SMA) indicator.
 * The SMA is calculated by summing the closing prices over a specified number of periods
 * and then dividing by the number of periods.
 */
public class SMA implements Indicator {
    private final int period;
    private final List<Number> values;
    private final List<Number> smaValues;

    /**
     * Constructs a new SMA indicator with the specified period.
     *
     * @param period the number of periods to use in the SMA calculation
     */
    public SMA(int period) {
        this.period = period;
        this.values = new ArrayList<>();
        this.smaValues = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     * Updates the SMA calculation with a new price bar.
     */
    @Override
    public void update(Bar bar) {
        values.add(bar.getClose());

        if (values.size() >= period) {
            BigDecimal sum = values.subList(values.size() - period, values.size()).stream()
                    .map(Number::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal average = sum.divide(BigDecimal.valueOf(period), Number.DECIMAL_PLACES, Number.ROUNDING_MODE);
            Number smaPrice = new Number(average);
            smaValues.add(smaPrice);
        } else {
            smaValues.add(Number.ZERO);
        }
    }

    /**
     * {@inheritDoc}
     * Returns the current SMA value.
     */
    @Override
    public Number getValue() {
        return smaValues.isEmpty() ? Number.ZERO : smaValues.getLast();
    }

    /**
     * {@inheritDoc}
     * Returns a historical SMA value. Index are 0-based, with 0 representing the most recent value.
     */
    @Override
    public Number getValue(int index) {
        if (index < 0 || index >= smaValues.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + smaValues.size());
        }
        return smaValues.get(smaValues.size() - index - 1);
    }

    /**
     * {@inheritDoc}
     * Returns the name of this SMA indicator, including the period.
     */
    @Override
    public String getName() {
        return "SMA " + period;
    }

    /**
     * {@inheritDoc}
     * Checks if the SMA has enough data to produce a valid value.
     */
    @Override
    public boolean isReady() {
        return values.size() >= period;
    }

    /**
     * {@inheritDoc}
     * Returns the number of periods required for this SMA.
     */
    @Override
    public int getRequiredPeriods() {
        return period;
    }
}