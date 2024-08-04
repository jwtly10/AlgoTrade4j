package dev.jwtly10.api.service;

import dev.jwtly10.core.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class StrategyWebSocketHandler extends TextWebSocketHandler {
    private final EventPublisher eventPublisher;
    private final Map<WebSocketSession, WebSocketEventListener> listeners = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> strategySessions = new ConcurrentHashMap<>();

    public StrategyWebSocketHandler(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New session established: {} ", session);
        WebSocketEventListener listener = new WebSocketEventListener(session);
        listeners.put(session, listener);
        eventPublisher.addListener(listener);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("Received message: {} ", message.getPayload());
        String payload = message.getPayload();
        if (payload.startsWith("STRATEGY:")) {
            String strategyId = payload.substring(9);
            log.info("Strategy ID: {} ", strategyId);
            strategySessions.put(strategyId, session);
            log.info("Updated strategySessions map. Size: {}", strategySessions.size());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        WebSocketEventListener listener = listeners.remove(session);
        if (listener != null) {
            eventPublisher.removeListener(listener);
        }
        strategySessions.values().remove(session);
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