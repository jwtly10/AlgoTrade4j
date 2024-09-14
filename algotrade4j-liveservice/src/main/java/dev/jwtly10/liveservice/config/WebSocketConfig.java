package dev.jwtly10.liveservice.config;

import dev.jwtly10.liveservice.service.websocket.LiveStrategyWSHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveStrategyWSHandler strategyWebSocketHandler;

    public WebSocketConfig(LiveStrategyWSHandler strategyWebSocketHandler) {
        this.strategyWebSocketHandler = strategyWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(strategyWebSocketHandler, "/ws/v1/strategy-events")
                .setAllowedOrigins("http://localhost:5173",
                        "https://algotrade4j.trade",
                        "https://www.algotrade4j.trade");
    }
}