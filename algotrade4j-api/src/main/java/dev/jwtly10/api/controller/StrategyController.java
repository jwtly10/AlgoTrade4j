package dev.jwtly10.api.controller;

import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.api.service.StrategyManager;
import dev.jwtly10.api.service.StrategyWebSocketHandler;
import dev.jwtly10.api.service.WebSocketEventListener;
import dev.jwtly10.core.event.*;
import dev.jwtly10.core.strategy.ParameterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/strategies")
@CrossOrigin(origins = "*")
@Slf4j
public class StrategyController {

    private final StrategyManager strategyManager;
    private final StrategyWebSocketHandler webSocketHandler;

    public StrategyController(StrategyManager strategyManager, StrategyWebSocketHandler webSocketHandler) {
        this.strategyManager = strategyManager;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startStrategy(@RequestBody StrategyConfig config, @RequestParam("strategyId") String strategyId) {
        log.debug("Starting strategy: {} with config : {}", strategyId, config);

        // We will retry this a few seconds
        WebSocketSession session = null;
        WebSocketEventListener listener = null;
        int maxAttempts = 25; // 5 seconds total (25 * 200ms)
        int attempts = 0;

        while (attempts < maxAttempts) {
            session = webSocketHandler.getSessionForStrategy(strategyId);
            if (session != null) {
                listener = webSocketHandler.getListenerForSession(session);
                if (listener != null) {
                    break;
                }
            }

            attempts++;
            try {
                Thread.sleep(200); // Wait for 200ms before retrying
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new StrategyManagerException("Interrupted while waiting for session/listener", StrategyManagerException.ErrorType.INTERNAL_ERROR);
            }
        }

        if (session == null) {
            log.error("Failed to start strategy: no session for ID {} after {} attempts", strategyId, attempts);
            throw new StrategyManagerException("Failed to start strategy: no session for ID " + strategyId, StrategyManagerException.ErrorType.BAD_REQUEST);
        }

        if (listener == null) {
            log.error("Failed to start strategy: no listener for session {} after {} attempts", session, attempts);
            throw new StrategyManagerException("Failed to start strategy: no listener for session " + session, StrategyManagerException.ErrorType.BAD_REQUEST);
        }

        subscribeToEvents(listener);

        log.debug("Starting strategy with config: {}", config);
        strategyManager.startStrategy(config, strategyId);
        log.info("Started strategy: {}", strategyId);


        return ResponseEntity.ok(strategyId);
    }

    private void subscribeToEvents(WebSocketEventListener listener) {
        listener.subscribe(BarEvent.class);
        listener.subscribe(TradeEvent.class);
        listener.subscribe(IndicatorEvent.class);
        listener.subscribe(StrategyStopEvent.class);
        listener.subscribe(AccountEvent.class);
        listener.subscribe(AnalysisEvent.class);
        listener.subscribe(LogEvent.class);
        listener.subscribe(ErrorEvent.class);
    }

    @PostMapping("/{strategyId}/stop")
    @Deprecated
    public ResponseEntity<String> stopStrategy(@PathVariable("strategyId") String strategyId) {
        log.debug("Stopping strategy: {}", strategyId);
        boolean stopped = strategyManager.stopStrategy(strategyId);
        if (stopped) {
            log.info("Stopped strategy: {}", strategyId);
            return ResponseEntity.ok("Stopped strategy: " + strategyId);
        } else {
            log.warn("Failed to stop strategy: {}", strategyId);
            throw new StrategyManagerException("Failed to stop strategy: " + strategyId, StrategyManagerException.ErrorType.BAD_REQUEST);
        }
    }

    @GetMapping("/{strategyClass}/params")
    public ResponseEntity<List<ParameterHandler.ParameterInfo>> getParamsFromStrategy(@PathVariable("strategyClass") String strategyClass) {
        log.debug("Getting parameters for strategy: {}", strategyClass);
        List<ParameterHandler.ParameterInfo> params = strategyManager.getParameters(strategyClass);
        log.debug("Parameters for strategy {}: {}", strategyClass, params);
        return ResponseEntity.ok(params);
    }

    @GetMapping
    public ResponseEntity<Set<String>> getStrategiesInSystem() {
        log.debug("Getting all strategies in the system");
        Set<String> strategies = strategyManager.getStrategiesInSystem();
        log.debug("Strategies in the system: {}", strategies);
        return ResponseEntity.ok(strategies);
    }

    @PostMapping("/generate-id")
    public ResponseEntity<String> generateStrategyId(@RequestBody StrategyConfig config) {
        String strategyId = strategyManager.generateStrategyId(config.getStrategyClass());
        return ResponseEntity.ok(strategyId);
    }

}