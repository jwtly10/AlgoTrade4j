package dev.jwtly10.liveservice.config;

import dev.jwtly10.liveservice.service.websocket.LiveStrategyWSHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static dev.jwtly10.shared.config.AllowedOrigins.ALLOWED_ORIGINS;

@Configuration
@EnableWebSocket
public class LiveWebSocketHandler implements WebSocketConfigurer {

    private final LiveStrategyWSHandler strategyWebSocketHandler;

    public LiveWebSocketHandler(LiveStrategyWSHandler strategyWebSocketHandler) {
        this.strategyWebSocketHandler = strategyWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(strategyWebSocketHandler, "/ws/v1/live-strategy-events")
                .setAllowedOrigins(ALLOWED_ORIGINS);
    }
}