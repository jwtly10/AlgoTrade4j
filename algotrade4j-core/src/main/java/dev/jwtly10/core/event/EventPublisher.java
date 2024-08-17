package dev.jwtly10.core.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * The EventPublisher class is responsible for managing event listeners and publishing events to them.
 * It implements a simple publish-subscribe pattern for event handling in the AlgoTrade4j framework.
 */
@Slf4j
public class EventPublisher {
    private final List<EventListener> listeners = new ArrayList<>();
    private final ExecutorService tickExecutor;


    public EventPublisher() {
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName("TickEventProcessor");
            return thread;
        };
        this.tickExecutor = Executors.newSingleThreadExecutor(threadFactory);
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
        log.debug("Publishing event: {}", event);
        for (EventListener listener : listeners) {
            tickExecutor.submit(() -> listener.onEvent(event));
        }
    }

    public void publishErrorEvent(String strategyId, Exception e) {
        for (EventListener listener : listeners) {
            tickExecutor.submit(() -> listener.onError(strategyId, e));
        }
    }

    public void shutdown() {
        tickExecutor.shutdown();
    }
}