package dev.jwtly10.core.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public abstract class BaseEvent {
    private final String id;
    private final String type;
    private final String symbol;
    private final Instant timestamp;
    private final String strategyId;

    protected BaseEvent(String strategyId, String type, String symbol) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.strategyId = strategyId;
        this.type = type;
        this.symbol = symbol;
    }
}