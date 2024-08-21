package dev.jwtly10.core.event.async;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

/**
 * Event representing all bars (BarSeries) in a strategy run
 */
@Getter
public class AsyncBarSeriesEvent extends BaseEvent {
    private final BarSeries barSeries;

    public AsyncBarSeriesEvent(String strategyId, Instrument instrument, BarSeries barSeries) {
        super(strategyId, "BAR_SERIES", instrument);
        this.barSeries = barSeries;
    }
}