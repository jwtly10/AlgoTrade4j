package dev.jwtly10.core.event;

public interface EventListener {
    void onEvent(BaseEvent event);

    void onError(String strategyId, Exception e);
}