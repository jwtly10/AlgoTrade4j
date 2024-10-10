package dev.jwtly10.core.event.types;

import dev.jwtly10.core.event.BaseEvent;
import lombok.Getter;

/**
 * Event representing an error in the system, that must be communicated to client
 * This event is similar to the {@link LogEvent}, however this event relates to the client, rather than strategy
 */
@Getter
public class ErrorEvent extends BaseEvent {
    /**
     * The error message associated with the error event.
     */
    private final String message;

    /**
     * Constructs an ErrorEvent with the specified strategy ID and message.
     *
     * @param strategyId the identifier of the strategy
     * @param message    the error message
     */
    public ErrorEvent(String strategyId, String message) {
        super(strategyId, "ERROR", null);
        this.message = message;
    }
}