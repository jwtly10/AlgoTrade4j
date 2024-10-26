package dev.jwtly10.core.event.types.async;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.event.Log;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

import java.util.List;

/**
 * Event representing all logs from a given strategy run
 * Only used for LiveStrategy implementations
 */
@Getter
public class AsyncLogsEvent extends BaseEvent {
    private final List<Log> logs;

    public AsyncLogsEvent(String strategyId, Instrument instrument, List<Log> logs) {
        super(strategyId, "ALL_LOGS", instrument);
        this.logs = logs;
    }
}