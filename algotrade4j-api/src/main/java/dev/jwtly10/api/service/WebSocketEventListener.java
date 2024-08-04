package dev.jwtly10.api.service;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.event.EventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class WebSocketEventListener implements EventListener {
    private final WebSocketSession session;
    private final Set<Class<? extends BaseEvent>> subscribedEventTypes = new HashSet<>();

    private final Object lock = new Object();

    public WebSocketEventListener(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(BaseEvent event) {
        if (subscribedEventTypes.contains(event.getClass())) {
            synchronized (lock) {
                try {
                    session.sendMessage(new TextMessage(event.toJson()));
                } catch (IOException e) {
                    log.error("Failed to send message to WS session", e);
                } catch (Exception e) {
                    log.error("Unexpected error sending message to WS session", e);
                }
            }
        }
    }

    @Override
    public void onError(String strategyId, Exception e) {
        try {
            session.sendMessage(new TextMessage("{\"error\": \"" + e.getMessage() + "\"}"));
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
}