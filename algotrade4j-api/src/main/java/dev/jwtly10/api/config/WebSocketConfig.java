package dev.jwtly10.api.config;

import dev.jwtly10.api.auth.config.JwtUtils;
import dev.jwtly10.api.auth.config.WebSocketAuthHandshakeInterceptor;
import dev.jwtly10.api.auth.service.UserDetailsServiceImpl;
import dev.jwtly10.api.service.StrategyWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final StrategyWebSocketHandler strategyWebSocketHandler;

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public WebSocketConfig(StrategyWebSocketHandler strategyWebSocketHandler, JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.strategyWebSocketHandler = strategyWebSocketHandler;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(strategyWebSocketHandler, "/ws/v1/strategy-events")
                .addInterceptors(new WebSocketAuthHandshakeInterceptor(jwtUtils, userDetailsService))
                .setAllowedOrigins("http://localhost:5173",
                        "https://algotrade4j.trade",
                        "http://localhost:3000",
                        "http://localhost",
                        "https://www.algotrade4j.trade");
    }
}