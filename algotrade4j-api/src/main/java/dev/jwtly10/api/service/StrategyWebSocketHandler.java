package dev.jwtly10.api.service;

import dev.jwtly10.core.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class StrategyWebSocketHandler extends TextWebSocketHandler {
    private final EventPublisher eventPublisher;
    private final StrategyManager strategyManager;
    private final Map<WebSocketSession, WebSocketEventListener> listeners = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> strategySessions = new ConcurrentHashMap<>();

    public StrategyWebSocketHandler(EventPublisher eventPublisher, StrategyManager strategyManager) {
        this.eventPublisher = eventPublisher;
        this.strategyManager = strategyManager;
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
            log.info("Strategy id: {} ", strategyId);
            strategySessions.put(strategyId, session);
            log.info("Updated strategySessions map. Size: {}", strategySessions.size());

            // Setup the listener for this strategy
            WebSocketEventListener listener = new WebSocketEventListener(session, strategyId);
            listeners.put(session, listener);
            eventPublisher.addListener(listener);


        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Session closed: {} ", session);
        // Stop the strategy on session closed
        Optional<String> strategyId = strategySessions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(session))
                .map(Map.Entry::getKey)
                .findFirst();

        strategyId.ifPresent(id -> {
            boolean stopped = retryStopStrategy(id);
            if (stopped) {
                log.info("Stopped strategy: {}", id);
                strategySessions.remove(id);
            } else {
                log.warn("Failed to stop strategy after retries: {}", id);
            }
        });

        WebSocketEventListener listener = listeners.get(session);
        if (listener != null) {
            listener.deactivate();
            listeners.remove(session);
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
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 3000; // 3 seconds timeout

        while (System.currentTimeMillis() < endTime) {
            if (strategyManager.stopStrategy(id)) {
                return true;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500); // Wait for 500 milliseconds before retrying
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Retry interrupted for strategy: {}", id);
                return false;
            }
        }
        return false;
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