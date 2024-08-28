package dev.jwtly10.core.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * The SyncEventPublisher class is a Synchronous event publisher.
 * This class should ONLY be used for very short-lived event handlers
 * It implements a simple publish-subscribe pattern for event handling internally
 */
@Slf4j
public class SyncEventPublisher implements EventPublisher {
    private final List<EventListener> listeners = new ArrayList<>();

    public SyncEventPublisher() {
    }

    /**
     * Adds an event listener to the list of subscribers.
     *
     * @param listener The EventListener to be added.
     */
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an event listener from the list of subscribers.
     *
     * @param listener The EventListener to be removed.
     */
    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Publishes an event to all registered listeners.
     * This method iterates through all registered listeners and calls their onEvent method,
     * passing the event as an argument.
     * Does not block the calling thread.
     *
     * @param event The BaseEvent to be published to all listeners.
     */
    public void publishEvent(BaseEvent event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public void publishErrorEvent(String strategyId, Exception e) {
        for (EventListener listener : listeners) {
            listener.onError(strategyId, e);
        }
    }

    public void shutdown() {
        // Not needed
    }
}