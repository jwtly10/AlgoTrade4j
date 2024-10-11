package dev.jwtly10.core.event.types;

import dev.jwtly10.core.event.BaseEvent;
import lombok.Getter;

/**
 * Event representing the stopping of a strategy.
 */
@Getter
public class StrategyStopEvent extends BaseEvent {
    /**
     * The reason for stopping the strategy.
     */
    private final String reason;

    /**
     * Constructs a StrategyStopEvent with the specified strategy ID and reason.
     *
     * @param strategyId the identifier of the strategy
     * @param reason     the reason for stopping the strategy
     */
    public StrategyStopEvent(String strategyId, String reason) {
        super(strategyId, "STRATEGY_STOP", null);
        this.reason = reason;
    }
}