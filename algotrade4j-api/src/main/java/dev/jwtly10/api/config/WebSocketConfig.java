package dev.jwtly10.api.config;

import dev.jwtly10.api.service.StrategyWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final StrategyWebSocketHandler strategyWebSocketHandler;

    public WebSocketConfig(StrategyWebSocketHandler strategyWebSocketHandler) {
        this.strategyWebSocketHandler = strategyWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(strategyWebSocketHandler, "/ws/v1/strategy-events")
                .setAllowedOrigins("*");
    }
}