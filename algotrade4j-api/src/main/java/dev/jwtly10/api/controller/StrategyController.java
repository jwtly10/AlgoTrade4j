package dev.jwtly10.api.controller;

import dev.jwtly10.api.exception.ErrorType;
import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.api.service.StrategyManager;
import dev.jwtly10.api.service.StrategyWebSocketHandler;
import dev.jwtly10.api.service.WebSocketEventListener;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.event.*;
import dev.jwtly10.core.event.async.*;
import dev.jwtly10.core.strategy.ParameterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Set;

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
    public ResponseEntity<String> startStrategy(
            @RequestBody StrategyConfig config,
            @RequestParam("strategyId") String strategyId,
            @RequestParam("async") boolean async,
            @RequestParam("showChart") boolean showChart
    ) {
        log.debug("Starting strategy: {} with config : {} and params: Async: {}, showChart: {}", strategyId, config, async, showChart);

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
                throw new StrategyManagerException("Interrupted while waiting for session/listener", ErrorType.INTERNAL_ERROR);
            }
        }

        if (session == null) {
            log.error("Failed to start strategy: no session for ID {} after {} attempts", strategyId, attempts);
            throw new StrategyManagerException("Failed to start strategy: no session for ID " + strategyId, ErrorType.BAD_REQUEST);
        }

        if (listener == null) {
            log.error("Failed to start strategy: no listener for session {} after {} attempts", session, attempts);
            throw new StrategyManagerException("Failed to start strategy: no listener for session " + session, ErrorType.BAD_REQUEST);
        }

        if (async) {
            // Ensures that the async run is instant
            config.setSpeed(DataSpeed.INSTANT);

            // Subscribe to async events
            subscribeToAsyncEvents(listener);
            // If no chart (For now only async runs will support no chart)
            // Then we don't need to send all bars, or indicator data
            if (!showChart) {
                listener.unsubscribe(AsyncBarSeriesEvent.class);
                listener.unsubscribe(AsyncIndicatorsEvent.class);
            }
        } else {
            subscribeToEvents(listener);
        }

        log.debug("Starting strategy with config: {}", config);
        try {
            strategyManager.startStrategy(config, strategyId);
        } catch (Exception e) {
            log.error("Error starting strategy: ", e);
            throw new StrategyManagerException("Error starting strategy: " + e, ErrorType.INTERNAL_ERROR);
        }
        log.info("Started strategy: {}", strategyId);

        return ResponseEntity.ok(strategyId);
    }

    /**
     * Adds support for 'async' strategy runs.
     * Clients can trigger a strategy run in the same way, but instead of being notified of every single event,
     * they will recieve all data once the run completes...
     * This was implemented as the React frontend cant keep up with the speed of the strategy runs,
     * Turning a 30 second backtest run on the server into a 10 minute visual chart.
     *
     * @param listener the websocket listener to subscribe to the async events
     */
    private void subscribeToAsyncEvents(WebSocketEventListener listener) {
        // ASYNC EVENTS
        listener.subscribe(AsyncBarSeriesEvent.class); // Sends bar data to fill chart
        listener.subscribe(AsyncTradesEvent.class); // Send list of all trade data to fill tables
        listener.subscribe(AsyncIndicatorsEvent.class); // Send list of all indicator data to fill chart
        listener.subscribe(AsyncAccountEvent.class); // Send account result at end of strategy
        listener.subscribe(AsyncProgressEvent.class); // Send progress while strategy runs

        // Standard Events
        listener.subscribe(AnalysisEvent.class);
        listener.subscribe(LogEvent.class);
        listener.subscribe(StrategyStopEvent.class);
    }

    /**
     * Generic subscription. Will subscribe to all the real time events the system generates
     *
     * @param listener the websocket listener to subscribe to the async events
     */
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
            throw new StrategyManagerException("Failed to stop strategy: " + strategyId, ErrorType.BAD_REQUEST);
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