package dev.jwtly10.liveapi.service.event;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.event.EventListener;
import dev.jwtly10.core.event.types.LogEvent;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyLogService;
import org.springframework.stereotype.Component;

/**
 * LogEventListener is an event listener that handles {@link LogEvent} instances.
 * <p>
 * It logs strategy events to the database using the provided {@link LiveStrategyLogService}.
 * This listener allows the system to process log events without modifying the core event
 * processing logic of the {@link dev.jwtly10.core.event.AsyncEventPublisher}.
 * </p>
 */
@Component
public class LogEventListener implements EventListener {
    private final LiveStrategyLogService liveStrategyLogService;

    public LogEventListener(LiveStrategyLogService liveStrategyLogService) {
        this.liveStrategyLogService = liveStrategyLogService;
    }

    /**
     * Called when an event is published.
     *
     * @param event The event that was published.
     */
    @Override
    public void onEvent(BaseEvent event) {
        if (event instanceof LogEvent) {
            LogEvent logEvent = (LogEvent) event;
            liveStrategyLogService.log(logEvent.getStrategyId(), logEvent.getLogType(), logEvent.getMessage());
        }
    }

    /**
     * Called when an error occurs.
     *
     * @param strategyId The ID of the strategy where the error occurred.
     * @param e          The error exception.
     */
    @Override
    public void onError(String strategyId, Exception e) {
        liveStrategyLogService.log(strategyId, LogEvent.LogType.ERROR, e.getMessage());
    }

    /**
     * Called when an error occurs.
     *
     * @param strategyId   The ID of the strategy where the error occurred.
     * @param errorMessage The error message.
     */
    @Override
    public void onError(String strategyId, String errorMessage) {
        liveStrategyLogService.log(strategyId, LogEvent.LogType.ERROR, errorMessage);
    }
}