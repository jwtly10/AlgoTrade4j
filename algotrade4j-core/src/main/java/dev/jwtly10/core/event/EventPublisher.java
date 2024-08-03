package dev.jwtly10.core.event;

import java.util.ArrayList;
import java.util.List;

public class EventPublisher {
    private final List<EventListener> listeners = new ArrayList<>();

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void publishEvent(BaseEvent event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}