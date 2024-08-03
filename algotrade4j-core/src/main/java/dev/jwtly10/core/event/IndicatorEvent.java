package dev.jwtly10.core.event;

import dev.jwtly10.core.Number;
import lombok.Getter;

@Getter
public class IndicatorEvent extends BaseEvent {
    private final String indicatorName;
    private final Number value;

    public IndicatorEvent(String strategyId, String symbol, String indicatorName, Number value) {
        super(strategyId, "INDICATOR", symbol);
        this.indicatorName = indicatorName;
        this.value = value;
    }
}