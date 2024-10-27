package dev.jwtly10.liveapi.service.event;

import dev.jwtly10.core.event.AsyncEventPublisher;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyLogService;

/**
 * LiveAsyncEventPublisher is an event publisher tailored for live trading scenarios.
 * <p>
 * This class extends {@link AsyncEventPublisher} and is designed to publish events asynchronously
 * in a live trading environment. Instead of overriding the internal event processing logic
 * (which could introduce concurrency issues or disrupt the event flow), it registers a custom
 * {@link LogEventListener} that specifically handles {@link dev.jwtly10.core.event.types.LogEvent}
 * instances and logs them to the database.
 * </p>
 */
public class LiveAsyncEventPublisher extends AsyncEventPublisher {

    /**
     * Constructs a new LiveAsyncEventPublisher and registers a LogEventListener to handle log events.
     *
     * @param liveStrategyLogService The service used to log strategy events to the database.
     */
    public LiveAsyncEventPublisher(LiveStrategyLogService liveStrategyLogService) {
        this.addListener(new LogEventListener(liveStrategyLogService));
    }
}