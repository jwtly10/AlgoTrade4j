package dev.jwtly10.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The EventPublisher class is responsible for managing event listeners and publishing events to them.
 * It implements a simple publish-subscribe pattern for event handling in the AlgoTrade4j framework.
 */
public class EventPublisher {
    private final List<EventListener> listeners = new ArrayList<>();
    private final ExecutorService executorService;


    public EventPublisher() {
        // Use virtual threads
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
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
     * Publishes an event to all registered listeners.
     * This method iterates through all registered listeners and calls their onEvent method,
     * passing the event as an argument.
     * Does not block the calling thread.
     *
     * @param event The BaseEvent to be published to all listeners.
     */
    public void publishEvent(BaseEvent event) {
        for (EventListener listener : listeners) {
            executorService.submit(() -> listener.onEvent(event));
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}