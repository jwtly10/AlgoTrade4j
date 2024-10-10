package dev.jwtly10.liveapi.service.event;

import dev.jwtly10.core.event.AsyncEventPublisher;
import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.event.types.LogEvent;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyLogService;

import java.util.ArrayList;
import java.util.List;

/**
 * Event publisher for live trading
 * <p>
 * This class is used to publish events asynchronously in a live trading environment.
 * However this has additional live trading specific functionality.
 * Including db logging of log events.
 * </p>
 */
public class LiveAsyncEventPublisher extends AsyncEventPublisher {

    private final LiveStrategyLogService liveStrategyLogService;

    public LiveAsyncEventPublisher(LiveStrategyLogService liveStrategyLogService) {
        this.liveStrategyLogService = liveStrategyLogService;
    }

    /**
     * Processes events in order, and log any log events
     */
    protected void processEvents() {
        List<BaseEvent> batch = new ArrayList<>(BATCH_SIZE);
        BaseEvent event;
        int processed = 0;
        while ((event = getEventQueue().poll()) != null && processed < BATCH_SIZE) {
            batch.add(event);
            processed++;
        }

        if (!batch.isEmpty()) {
            for (BaseEvent baseEvent : batch) {
                if (baseEvent instanceof LogEvent) {
                    LogEvent logEvent = (LogEvent) baseEvent;
                    liveStrategyLogService.log(logEvent.getStrategyId(), logEvent.getLogType(), logEvent.getMessage());
                }
            }

            super.processEvents();
        }
    }
}