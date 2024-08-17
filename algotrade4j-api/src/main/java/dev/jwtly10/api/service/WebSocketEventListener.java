package dev.jwtly10.api.service;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.event.ErrorEvent;
import dev.jwtly10.core.event.EventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class WebSocketEventListener implements EventListener {
    private final WebSocketSession session;
    private final Set<Class<? extends BaseEvent>> subscribedEventTypes = new HashSet<>();
    private final AtomicBoolean isActive = new AtomicBoolean(true);
    private final Object lock = new Object();
    private final String strategyId;

    public WebSocketEventListener(WebSocketSession session, String strategyId) {
        this.session = session;
        this.strategyId = strategyId;
    }

    @Override
    public void onEvent(BaseEvent event) {
        if (!isActive.get() || !subscribedEventTypes.contains(event.getClass()) || !event.getStrategyId().equals(this.strategyId)) {
            return;
        }

        synchronized (lock) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(event.toJson()));
                }
            } catch (IllegalStateException e) {
                log.debug("Session already closed, unable to send event: {}", event);
                deactivate();
            } catch (IOException e) {
                log.error("Failed to send message to WS session", e);
            }
        }
    }

    @Override
    public void onError(String strategyId, Exception e) {
        try {
            session.sendMessage(new TextMessage(new ErrorEvent(strategyId, e.getMessage()).toJson()));
        } catch (IOException ex) {
            log.error("Failed to send error message to WS session", ex);
        } catch (Exception ex) {
            log.error("Unexpected error sending error message to WS session", ex);
        }
    }

    public void subscribe(Class<? extends BaseEvent> eventType) {
        subscribedEventTypes.add(eventType);
    }

    public void unsubscribe(Class<? extends BaseEvent> eventType) {
        subscribedEventTypes.remove(eventType);
    }

    public void deactivate() {
        isActive.set(false);
    }
}