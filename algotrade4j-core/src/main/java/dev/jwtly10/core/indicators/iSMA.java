package dev.jwtly10.core.indicators;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.IndicatorEvent;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.IndicatorValue;
import dev.jwtly10.core.model.Number;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a Simple Moving Average (SMA) indicator.
 * The SMA is calculated by summing the closing prices over a specified number of periods
 * and then dividing by the number of periods.
 */
@Slf4j
public class iSMA implements Indicator {
    // Params
    private final int period;

    private final List<Double> rawValues; // The raw running sum
    @Getter
    private final List<IndicatorValue> values; // The data the indicator produces
    private final String name;
    private String strategyId;
    private EventPublisher eventPublisher;

    /**
     * Constructs a new SMA indicator with the specified period.
     *
     * @param period the number of periods to use in the SMA calculation
     */
    public iSMA(int period) {
        this.period = period;
        this.rawValues = new ArrayList<>();
        this.values = new ArrayList<>();
        this.name = "SMA " + period;
    }

    /**
     * {@inheritDoc}
     * Updates the SMA calculation with a new price bar.
     */
    @Override
    public void update(Bar bar) {
        log.trace("Updating SMA with new bar. Close price: {}", bar.getClose());
        rawValues.add(bar.getClose().getValue().doubleValue());

        if (isReady()) {
            BigDecimal sum = rawValues.subList(rawValues.size() - period, rawValues.size()).stream()
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal average = sum.divide(BigDecimal.valueOf(period), Number.DECIMAL_PLACES, Number.ROUNDING_MODE);
            IndicatorValue indicatorValue = new IndicatorValue(average.doubleValue(), bar.getOpenTime());
            values.add(indicatorValue);
            if (eventPublisher != null) {
                log.trace("Publishing SMA event. Strategy ID: {}, Symbol: {}, Indicator: {}, Value: {}, Timestamp: {}",
                        strategyId, bar.getInstrument(), getName(), average.doubleValue(), bar.getOpenTime());
                eventPublisher.publishEvent(new IndicatorEvent(strategyId, bar.getInstrument(), getName(), indicatorValue));
            }
        } else {
            IndicatorValue indicatorValue = new IndicatorValue(0, bar.getOpenTime());
            values.add(indicatorValue);
            if (eventPublisher != null) {
                eventPublisher.publishEvent(new IndicatorEvent(strategyId, bar.getInstrument(), getName(), indicatorValue));
            }
        }
    }

    /**
     * {@inheritDoc}
     * Returns the current SMA value.
     */
    @Override
    public double getValue() {
        return values.isEmpty() ? 0 : values.getLast().getValue();
    }

    /**
     * {@inheritDoc}
     * Returns a historical SMA value. Index are 0-based, with 0 representing the most recent value.
     */
    @Override
    public double getValue(int index) {
        if (index < 0 || index >= values.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + values.size());
        }
        return values.get(values.size() - index - 1).getValue();
    }

    /**
     * {@inheritDoc}
     * Returns the name of this SMA indicator, including the period.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     * Checks if the SMA has enough data to produce a valid value.
     */
    @Override
    public boolean isReady() {
        log.trace("Checking if SMA is ready. Values size: {}, Period: {}", rawValues.size(), period);
        return rawValues.size() >= period;
    }

    /**
     * {@inheritDoc}
     * Returns the number of periods required for this SMA.
     */
    @Override
    public int getRequiredPeriods() {
        return period;
    }

    /**
     * {@inheritDoc}
     * Sets the event publisher for the SMA.
     */
    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * {@inheritDoc}
     * Sets the strategy id
     */
    @Override
    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

}