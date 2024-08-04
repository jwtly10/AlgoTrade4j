package dev.jwtly10.api.controller;

import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.api.service.StrategyManager;
import dev.jwtly10.api.service.StrategyWebSocketHandler;
import dev.jwtly10.api.service.WebSocketEventListener;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.TradeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;

@RestController
@RequestMapping("/api/v1/strategies")
@Slf4j
public class StrategyController {

    private final StrategyManager strategyManager;
    private final StrategyWebSocketHandler webSocketHandler;

    public StrategyController(StrategyManager strategyManager, StrategyWebSocketHandler webSocketHandler) {
        this.strategyManager = strategyManager;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startStrategy(@RequestBody StrategyConfig config) {
        log.debug("Starting strategy with config: {}", config);
        String strategyId = strategyManager.startStrategy(config);
        log.info("Started strategy: {}", strategyId);

        WebSocketSession session = webSocketHandler.getSessionForStrategy(strategyId);
        log.debug("Session for strategy: {}", session);
        if (session != null) {
            WebSocketEventListener listener = webSocketHandler.getListenerForSession(session);
            if (listener != null) {
                for (String eventType : config.getSubscriptions()) {
                    switch (eventType) {
                        case "BAR":
                            listener.subscribe(BarEvent.class);
                            break;
                        case "TRADE":
                            listener.subscribe(TradeEvent.class);
                            break;
                    }
                }
            }
        } else {
            return ResponseEntity.badRequest().body("No active WebSocket session found for the strategy");
        }

        return ResponseEntity.ok(strategyId);
    }
}