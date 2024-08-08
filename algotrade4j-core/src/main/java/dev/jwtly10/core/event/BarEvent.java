package dev.jwtly10.core.event;

import dev.jwtly10.core.model.Bar;
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
     * Constructs a BarEvent with the specified strategy ID, symbol, and bar.
     *
     * @param strategyId the identifier of the strategy
     * @param symbol     the symbol associated with the bar
     * @param bar        the bar associated with the event
     */
    public BarEvent(String strategyId, String symbol, Bar bar) {
        super(strategyId, "BAR", symbol);
        this.bar = bar;
    }
}