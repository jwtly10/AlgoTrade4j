package dev.jwtly10.core.event;

import dev.jwtly10.core.model.Number;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class IndicatorEvent extends BaseEvent {
    private final String indicatorName;
    private final Number value;
    private final ZonedDateTime dateTime;

    public IndicatorEvent(String strategyId, String symbol, String indicatorName, Number value, ZonedDateTime dateTime) {
        super(strategyId, "INDICATOR", symbol);
        this.indicatorName = indicatorName;
        this.value = value;
        this.dateTime = dateTime;
    }
}