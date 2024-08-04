package dev.jwtly10.core.event;

import lombok.Getter;

@Getter
public class StrategyStopEvent extends BaseEvent {
    private final String reason;

    public StrategyStopEvent(String strategyId, String reason) {
        super(strategyId, "STRATEGY_STOP", null);
        this.reason = reason;
    }
}