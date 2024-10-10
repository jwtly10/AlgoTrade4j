package dev.jwtly10.core.event.types;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Trade;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Event representing a trade action in the system.
 */
@Getter
public class TradeEvent extends BaseEvent {
    /**
     * The trade associated with the event.
     */
    private final Trade trade;

    /**
     * The action performed on the trade.
     */
    private final Action action;

    /**
     * Constructs a TradeEvent with the specified strategy ID, instrument, trade, and action.
     *
     * @param strategyId the identifier of the strategy
     * @param instrument the instrument associated with the trade
     * @param trade      the trade associated with the event
     * @param action     the action performed on the trade
     */
    public TradeEvent(String strategyId, Instrument instrument, Trade trade, Action action) {
        super(strategyId, "TRADE", instrument);
        this.trade = trade;
        this.action = action;
    }

    public TradeEvent(String strategyId, Instrument instrument, Trade trade, Action action, ZonedDateTime timestamp) {
        super(strategyId, "TRADE", instrument, timestamp);
        this.trade = trade;
        this.action = action;
    }

    /**
     * Enum representing the possible actions on a trade.
     */
    public enum Action {
        /**
         * Action indicating the opening of a trade.
         */
        OPEN,

        /**
         * Action indicating the closing of a trade.
         */
        CLOSE,

        /**
         * Action indicating the update of a trade.
         */
        UPDATE
    }
}