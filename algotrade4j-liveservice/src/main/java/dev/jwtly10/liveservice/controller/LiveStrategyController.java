package dev.jwtly10.liveservice.controller;

import dev.jwtly10.liveservice.model.LiveStrategy;
import dev.jwtly10.liveservice.service.strategy.LiveStrategyManager;
import dev.jwtly10.liveservice.service.strategy.LiveStrategyService;
import dev.jwtly10.shared.config.ratelimit.RateLimit;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/live/strategies")
public class LiveStrategyController {

    private final LiveStrategyService liveStrategyService;

    private final LiveStrategyManager liveStrategyManager;

    public LiveStrategyController(LiveStrategyService liveStrategyService, LiveStrategyManager liveStrategyManager) {
        this.liveStrategyService = liveStrategyService;
        this.liveStrategyManager = liveStrategyManager;
    }

    @GetMapping
    public ResponseEntity<List<LiveStrategy>> getLiveStrategies() {
        List<LiveStrategy> liveStrategies = liveStrategyService.getNonHiddenLiveStrategies();
        return ResponseEntity.ok(liveStrategies);
    }

    @PostMapping
    @RateLimit(limit = 3)
    public ResponseEntity<LiveStrategy> createLiveStrategy(
            @RequestBody LiveStrategy strategySetup) {
        LiveStrategy newLiveStrategy = liveStrategyService.createLiveStrategy(strategySetup);
        return ResponseEntity.ok(newLiveStrategy);
    }

    @PutMapping("/{id}")
    @RateLimit(limit = 3)
    public ResponseEntity<LiveStrategy> updateLiveStrategy(
            @RequestBody LiveStrategy strategySetup,
            @PathVariable("id") Long id
    ) {
        LiveStrategy updatedLiveStrategy = liveStrategyService.updateLiveStrategy(id, strategySetup);
        return ResponseEntity.ok(updatedLiveStrategy);
    }

    @PostMapping("/{id}/toggle")
    @RateLimit(limit = 5)
    public ResponseEntity<LiveStrategy> toggleLiveStrategy(@PathVariable("id") Long id) {
        LiveStrategy activatedLiveStrategy = liveStrategyService.toggleStrategy(id);

        // If the strategy is now active. Try to run it.
        try {
            if (activatedLiveStrategy.isActive()) {
                liveStrategyManager.startStrategy(activatedLiveStrategy);
            } else {
                liveStrategyManager.stopStrategy(activatedLiveStrategy.getStrategyName());
            }
        } catch (Exception e) {
            // If there is an error starting the strategy, set the error message and force deactivate the strategy
            liveStrategyService.setErrorMessage(activatedLiveStrategy, e.getMessage());
            liveStrategyService.deactivateStrategy(activatedLiveStrategy.getStrategyName());
            throw new ApiException("Error starting strategy. See strategy Live Alert for reason.", ErrorType.INTERNAL_ERROR);
        }

        return ResponseEntity.ok(activatedLiveStrategy);
    }

    @DeleteMapping("/{id}")
    @RateLimit(limit = 5)
    public ResponseEntity<Void> deleteLiveStrategy(@PathVariable("id") Long id) {
        liveStrategyService.deleteStrategy(id);
        return ResponseEntity.ok().build();
    }
}