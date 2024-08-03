package dev.jwtly10.core.event;

import dev.jwtly10.core.Trade;
import lombok.Getter;

@Getter
public class TradeEvent extends BaseEvent {
    private final Trade trade;
    private final Action action;

    public TradeEvent(String strategyId, String symbol, Trade trade, Action action) {
        super(strategyId, "TRADE", symbol);
        this.trade = trade;
        this.action = action;
    }

    public enum Action {
        OPEN, CLOSE, UPDATE
    }
}