package dev.jwtly10.core.event;

import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import lombok.Getter;

import java.time.ZonedDateTime;

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
    private final Number value;

    /**
     * The date and time of the indicator event.
     */
    private final ZonedDateTime dateTime;

    /**
     * Constructs an IndicatorEvent with the specified strategy ID, instrument, indicator name, value, and date/time.
     *
     * @param strategyId    the identifier of the strategy
     * @param instrument    the instrument associated with the indicator
     * @param indicatorName the name of the indicator
     * @param value         the value of the indicator
     * @param dateTime      the date and time of the indicator value
     */
    public IndicatorEvent(String strategyId, Instrument instrument, String indicatorName, Number value, ZonedDateTime dateTime) {
        super(strategyId, "INDICATOR", instrument);
        this.indicatorName = indicatorName;
        this.value = value;
        this.dateTime = dateTime;
    }
}