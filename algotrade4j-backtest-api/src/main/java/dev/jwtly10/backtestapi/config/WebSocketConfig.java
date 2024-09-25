package dev.jwtly10.backtestapi.config;

import dev.jwtly10.backtestapi.service.websocket.StrategyWebSocketHandler;
import dev.jwtly10.shared.auth.filter.WebSocketAuthHandshakeInterceptor;
import dev.jwtly10.shared.auth.service.UserDetailsServiceImpl;
import dev.jwtly10.shared.auth.utils.JwtUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static dev.jwtly10.shared.config.AllowedOrigins.ALLOWED_ORIGINS;

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
                .setAllowedOrigins(ALLOWED_ORIGINS);
    }
}