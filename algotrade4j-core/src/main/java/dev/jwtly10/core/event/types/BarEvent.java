package dev.jwtly10.core.event.types;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

/**
 * Event representing a bar action in the system.
 */
@Getter
public class BarEvent extends BaseEvent {
    /**
     * The bar associated with the event.
     */
    private final Bar bar;

    /**
     * Constructs a BarEvent with the specified strategy ID, instrument, and bar.
     *
     * @param strategyId the identifier of the strategy
     * @param instrument the instrument associated with the bar
     * @param bar        the bar associated with the event
     */
    public BarEvent(String strategyId, Instrument instrument, Bar bar) {
        super(strategyId, "BAR", instrument);
        this.bar = bar;
    }
}