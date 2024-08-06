package dev.jwtly10.core.event;

import dev.jwtly10.core.model.Bar;
import lombok.Getter;

@Getter
public class BarEvent extends BaseEvent {
    private final Bar bar;

    public BarEvent(String strategyId, String symbol, Bar bar) {
        super(strategyId, "BAR", symbol);
        this.bar = bar;
    }
}