package dev.jwtly10.core.event.types;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.model.IndicatorValue;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

/**
 * Event representing an indicator action in the system.
 */
@Getter
public class IndicatorEvent extends BaseEvent {
    /**
     * The name of the indicator.
     */
    private final String indicatorName;

    /**
     * The value of the indicator.
     */
    private final IndicatorValue value;

    /**
     * Constructs an IndicatorEvent with the specified strategy ID, instrument, indicator name, value, and date/time.
     *
     * @param strategyId    the identifier of the strategy
     * @param instrument    the instrument associated with the indicator
     * @param indicatorName the name of the indicator
     * @param value         the value of the indicator (inlc dateTime)
     */
    public IndicatorEvent(String strategyId, Instrument instrument, String indicatorName, IndicatorValue value) {
        super(strategyId, "INDICATOR", instrument);
        this.indicatorName = indicatorName;
        this.value = value;
    }
}