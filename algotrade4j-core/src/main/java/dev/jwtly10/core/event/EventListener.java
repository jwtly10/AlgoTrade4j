package dev.jwtly10.core.event;

/**
 * Interface for handling events and errors from the system.
 * Can be implemented by external classes to receive event notifications.
 */
public interface EventListener {
    /**
     * Called when an event occurs.
     *
     * @param event the event that occurred
     */
    void onEvent(BaseEvent event);

    /**
     * Called when an error occurs.
     *
     * @param strategyId the identifier of the strategy associated with the error
     * @param e          the exception that occurred
     */
    void onError(String strategyId, Exception e);
}