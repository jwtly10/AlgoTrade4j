package dev.jwtly10.core.event;

/**
 * The EventPublisher interface defines the contract for any EventPublisher implementations
 */
public interface EventPublisher {
    /**
     * Adds an event listener to the list of subscribers.
     *
     * @param listener The EventListener to be added.
     */
    void addListener(EventListener listener);

    /**
     * Removes an event listener from the list of subscribers.
     *
     * @param listener The EventListener to be removed.
     */
    void removeListener(EventListener listener);

    /**
     * Publishes an event to all registered listeners.
     * This method iterates through all registered listeners and calls their onEvent method,
     * passing the event as an argument.
     * Does not block the calling thread.
     *
     * @param event The BaseEvent to be published to all listeners.
     */
    void publishEvent(BaseEvent event);

    /**
     * Publishes an error event to all registered listeners
     *
     * @param strategyId the strategy id where the error happened
     * @param e          the exception
     */
    void publishErrorEvent(String strategyId, Exception e);

    void shutdown();
}