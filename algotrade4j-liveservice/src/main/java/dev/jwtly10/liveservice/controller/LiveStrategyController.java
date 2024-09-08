package dev.jwtly10.liveservice.controller;

import dev.jwtly10.liveservice.model.LiveStrategy;
import dev.jwtly10.liveservice.model.LiveStrategyConfig;
import dev.jwtly10.liveservice.service.LiveStrategyService;
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
        return null;
    }

    @PostMapping
    public ResponseEntity<LiveStrategy> createLiveStrategy(
            @RequestBody LiveStrategyConfig config,
            @RequestParam("strategyName") String strategyName) {
        LiveStrategy newLiveStrategy = liveStrategyService.createLiveStrategy(config, strategyName);
        return ResponseEntity.ok(newLiveStrategy);
    }

    @PostMapping("/{id}")
    public ResponseEntity<LiveStrategy> toggleLiveStrategy(@PathVariable Long id) {
        LiveStrategy activatedLiveStrategy = liveStrategyService.toggleStrategy(id);
        return ResponseEntity.ok(activatedLiveStrategy);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiveStrategy(@PathVariable Long id) {
        liveStrategyService.deleteStrategy(id);
        return ResponseEntity.ok().build();
    }
}