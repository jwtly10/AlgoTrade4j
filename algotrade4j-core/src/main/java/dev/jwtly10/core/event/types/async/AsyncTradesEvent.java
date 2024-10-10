package dev.jwtly10.core.event.types.async;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Trade;
import lombok.Getter;

import java.util.Map;

/**
 * Event representing all trade data from a given strategy run
 */
@Getter
public class AsyncTradesEvent extends BaseEvent {
    private final Map<Integer, Trade> trades;

    public AsyncTradesEvent(String strategyId, Instrument instrument, Map<Integer, Trade> trades) {
        super(strategyId, "ALL_TRADES", instrument);
        this.trades = trades;
    }
}