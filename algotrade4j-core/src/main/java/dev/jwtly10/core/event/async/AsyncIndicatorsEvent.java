package dev.jwtly10.core.event.async;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.model.IndicatorValue;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Event representing all indicator data from a given strategy run
 */
@Getter
public class AsyncIndicatorsEvent extends BaseEvent {
    private final Map<String, List<IndicatorValue>> indicators;

    public AsyncIndicatorsEvent(String strategyId, Instrument instrument, Map<String, List<IndicatorValue>> indicators) {
        super(strategyId, "ALL_INDICATORS", instrument);
        this.indicators = indicators;
    }
}