package dev.jwtly10.core.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The AsyncEventPublisher class is the main vehicle responsible for managing event listeners and publishing events to them in a performant way.
 * This works asynchronously and will batch requests for better performance. May not be suitable for instances where handles require synchronous handling
 */
@Slf4j
public class AsyncEventPublisher implements EventPublisher {
    private static final int BATCH_SIZE = 50;
    private static final long MAX_BATCH_WAIT_MS = 100;
    private final List<EventListener> listeners = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Using a queue to batch send events rather than creating a new runnable instant for each invocation
    private final Queue<BaseEvent> eventQueue = new ConcurrentLinkedQueue<>();

    public AsyncEventPublisher() {
        scheduler.scheduleAtFixedRate(this::processEvents, 0, MAX_BATCH_WAIT_MS, TimeUnit.MILLISECONDS);
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
        eventQueue.offer(event);
        if (eventQueue.size() >= BATCH_SIZE) {
            processEvents();
        }
    }

    /**
     * Processes events in order.
     */
    private void processEvents() {
        List<BaseEvent> batch = new ArrayList<>(BATCH_SIZE);
        BaseEvent event;
        while ((event = eventQueue.poll()) != null && batch.size() < BATCH_SIZE) {
            batch.add(event);
        }

        if (!batch.isEmpty()) {
            for (EventListener listener : listeners) {
                for (BaseEvent e : batch) {
                    listener.onEvent(e);
                }
            }
        }
    }

    /**
     * Publishes an error event
     *
     * @param strategyId the strategy id where the error happened
     * @param e          the exception
     */
    public void publishErrorEvent(String strategyId, Exception e) {
        for (EventListener listener : listeners) {
            try {
                listener.onError(strategyId, e);
            } catch (Exception listenerException) {
                log.error("Error in listener while processing error event", listenerException);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}