package dev.jwtly10.liveapi.service.websocket;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.Log;
import dev.jwtly10.core.event.types.*;
import dev.jwtly10.core.event.types.async.AsyncBarSeriesEvent;
import dev.jwtly10.core.event.types.async.AsyncIndicatorsEvent;
import dev.jwtly10.core.event.types.async.AsyncLogsEvent;
import dev.jwtly10.core.event.types.async.AsyncTradesEvent;
import dev.jwtly10.liveapi.executor.LiveExecutor;
import dev.jwtly10.liveapi.model.strategy.LiveStrategyLog;
import dev.jwtly10.liveapi.repository.LiveExecutorRepository;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class LiveStrategyWSHandler extends TextWebSocketHandler {
    private final EventPublisher eventPublisher;
    private final Map<WebSocketSession, WebSocketEventListener> listeners = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> strategySessions = new ConcurrentHashMap<>();
    private final LiveExecutorRepository liveExecutorRepository;
    private final LiveStrategyLogService liveStrategyLogService;

    public LiveStrategyWSHandler(EventPublisher eventPublisher, LiveExecutorRepository liveExecutorRepository, LiveStrategyLogService liveStrategyLogService) {
        this.eventPublisher = eventPublisher;
        this.liveExecutorRepository = liveExecutorRepository;
        this.liveStrategyLogService = liveStrategyLogService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New session established: {} ", session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("Received message: {} ", message.getPayload());
        String payload = message.getPayload();
        if (payload.startsWith("STRATEGY:")) {
            String strategyId = payload.substring(9);
            log.info("Starting WS connection for live strategy: '{}'", strategyId);
            LiveExecutor executor = liveExecutorRepository.getStrategy(strategyId);
            if (executor != null) {
                WebSocketEventListener listener = new WebSocketEventListener(session, strategyId);
                listeners.put(session, listener);
                eventPublisher.addListener(listener);
                listener.subscribe(BarEvent.class);
                listener.subscribe(TradeEvent.class);
                listener.subscribe(IndicatorEvent.class);
                listener.subscribe(StrategyStopEvent.class);
                listener.subscribe(AccountEvent.class);
                listener.subscribe(AnalysisEvent.class);
                listener.subscribe(LiveAnalysisEvent.class);
                listener.subscribe(LogEvent.class);
                listener.subscribe(AsyncLogsEvent.class);
                listener.subscribe(ErrorEvent.class);
                listener.subscribe(AsyncIndicatorsEvent.class);
                listener.subscribe(AsyncTradesEvent.class);
                sendInitialState(session, executor);
            } else {
                try {
                    session.sendMessage(new TextMessage("ERROR:" + message));
                } catch (IOException e) {
                    log.error("Error sending error message: {}", e.getMessage(), e);
                }
            }
        }
    }


    private void sendInitialState(WebSocketSession session, LiveExecutor executor) {
        try {
            WebSocketEventListener listener = listeners.get(session);
            AsyncBarSeriesEvent barSeriesEvent = new AsyncBarSeriesEvent(executor.getStrategyId(), executor.getInstrument(), executor.getBarSeries());
            listener.sendBinaryMessage(barSeriesEvent.toJson());
            AsyncTradesEvent tradesEvent = new AsyncTradesEvent(executor.getStrategyId(), executor.getInstrument(), executor.getTrades());
            listener.sendBinaryMessage(tradesEvent.toJson());
            AsyncIndicatorsEvent indicatorsEvent = new AsyncIndicatorsEvent(executor.getStrategyId(), executor.getInstrument(), executor.getIndicators());
            listener.sendBinaryMessage(indicatorsEvent.toJson());

            List<LiveStrategyLog> dbLogs = liveStrategyLogService.getLogs(executor.getStrategyId());
            List<Log> logs = dbLogs.stream().map(log ->
                    new Log(
                            log.getMessage(),
                            LogEvent.LogType.valueOf(log.getLevel()),
                            log.getCreatedAt())
            ).toList();
            AsyncLogsEvent logsEvent = new AsyncLogsEvent(executor.getStrategyId(), executor.getInstrument(), logs);
            listener.sendBinaryMessage(logsEvent.toJson());
        } catch (IOException e) {
            log.error("Error sending initial state: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Session closed: {} ", session);
        WebSocketEventListener listener = listeners.remove(session);
        if (listener != null) {
            eventPublisher.removeListener(listener);
        }
    }

    /**
     * Attempts to strop the strategy for 3 seconds.
     * This handles cases where a stop request (or disconnect happens before the strategy has actually initialised
     * Gracefully shutting it down without running in the background without a client attached
     *
     * @param id the id of the strategy t ostop
     * @return true is the strategy was successfully stopped.
     */
    private boolean retryStopStrategy(String id) {
        return false;
//        long startTime = System.currentTimeMillis();
//        long endTime = startTime + 3000; // 3 seconds timeout
//
//        while (System.currentTimeMillis() < endTime) {
//            if (liveStrategyManager.stopStrategy(id)) {
//                return true;
//            }
//            try {
//                TimeUnit.MILLISECONDS.sleep(500); // Wait for 500 milliseconds before retrying
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                log.warn("Retry interrupted for strategy: {}", id);
//                return false;
//            }
//        }
//        return false;
    }

    public WebSocketSession getSessionForStrategy(String strategyId) {
        log.debug("Finding strategy for: {} ", strategyId);
        log.debug("Current strategySessions map: {}", strategySessions);
        return strategySessions.get(strategyId);
    }

    public WebSocketEventListener getListenerForSession(WebSocketSession session) {
        log.debug("Getting listeners for: {} ", session);
        return listeners.get(session);
    }
}