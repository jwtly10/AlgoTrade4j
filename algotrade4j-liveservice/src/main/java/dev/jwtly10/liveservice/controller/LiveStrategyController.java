package dev.jwtly10.liveservice.controller;

import dev.jwtly10.liveservice.model.LiveStrategy;
import dev.jwtly10.liveservice.service.strategy.LiveStrategyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/live/strategies")
public class LiveStrategyController {

    private final LiveStrategyService liveStrategyService;

    public LiveStrategyController(LiveStrategyService liveStrategyService) {
        this.liveStrategyService = liveStrategyService;
    }

    @GetMapping
    public ResponseEntity<List<LiveStrategy>> getLiveStrategies() {
        List<LiveStrategy> liveStrategies = liveStrategyService.getNonHiddenLiveStrategies();
        return ResponseEntity.ok(liveStrategies);
    }

    @PostMapping
    public ResponseEntity<LiveStrategy> createLiveStrategy(
            @RequestBody LiveStrategy strategySetup) {
        LiveStrategy newLiveStrategy = liveStrategyService.createLiveStrategy(strategySetup);
        return ResponseEntity.ok(newLiveStrategy);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LiveStrategy> updateLiveStrategy(
            @RequestBody LiveStrategy strategySetup,
            @PathVariable("id") Long id
    ) {
        LiveStrategy updatedLiveStrategy = liveStrategyService.updateLiveStrategy(id, strategySetup);
        return ResponseEntity.ok(updatedLiveStrategy);
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<LiveStrategy> toggleLiveStrategy(@PathVariable("id") Long id) {
        LiveStrategy activatedLiveStrategy = liveStrategyService.toggleStrategy(id);
        return ResponseEntity.ok(activatedLiveStrategy);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiveStrategy(@PathVariable("id") Long id) {
        liveStrategyService.deleteStrategy(id);
        return ResponseEntity.ok().build();
    }
}