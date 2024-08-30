package dev.jwtly10.core.indicators;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.IndicatorEvent;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.IndicatorValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements an Exponential Moving Average (EMA) indicator.
 * The EMA gives more weight to recent prices, making it more responsive to new information.
 */
@Slf4j
public class iEMA implements Indicator {
    // Params
    private final int period;
    private final double multiplier;

    @Getter
    private final List<IndicatorValue> values; // The data the indicator produces
    private final String name;
    private String strategyId;
    private EventPublisher eventPublisher;

    /**
     * Constructs a new EMA indicator with the specified period.
     *
     * @param period the number of periods to use in the EMA calculation
     */
    public iEMA(int period) {
        this.period = period;
        this.multiplier = (2.0 / (period + 1));
        this.values = new ArrayList<>();
        this.name = "EMA " + period;
    }

    /**
     * {@inheritDoc}
     * Updates the EMA calculation with a new price bar.
     */
    @Override
    public void update(Bar bar) {
        log.trace("Updating EMA with new bar. Close price: {}", bar.getClose());

        if (values.isEmpty()) {
            // First value is treated as SMA
            IndicatorValue indicatorValue = new IndicatorValue(bar.getClose().getValue().doubleValue(), bar.getOpenTime());
            values.add(indicatorValue);
        } else {
            double previousEMA = values.getLast().getValue();
            double newEMA = (bar.getClose().getValue().doubleValue() * multiplier) + (previousEMA * (1 - multiplier));
            IndicatorValue indicatorValue = new IndicatorValue(newEMA, bar.getOpenTime());
            values.add(indicatorValue);
        }

        if (eventPublisher != null) {
            log.trace("Publishing EMA event. Strategy ID: {}, Symbol: {}, Indicator: {}, Value: {}, Timestamp: {}",
                    strategyId, bar.getInstrument(), getName(), getValue(), bar.getOpenTime());
            eventPublisher.publishEvent(new IndicatorEvent(strategyId, bar.getInstrument(), getName(), values.get(values.size() - 1)));
        }
    }

    /**
     * {@inheritDoc}
     * Returns the current EMA value.
     */
    @Override
    public double getValue() {
        return values.isEmpty() ? 0 : values.getLast().getValue();
    }

    /**
     * {@inheritDoc}
     * Returns a historical EMA value. Index are 0-based, with 0 representing the most recent value.
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
     * Returns the name of this EMA indicator, including the period.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     * Checks if the EMA has enough data to produce a valid value.
     */
    @Override
    public boolean isReady() {
        return !values.isEmpty();
    }

    /**
     * {@inheritDoc}
     * Returns the number of periods required for this EMA.
     */
    @Override
    public int getRequiredPeriods() {
        return period;
    }

    /**
     * {@inheritDoc}
     * Sets the event publisher for the EMA.
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