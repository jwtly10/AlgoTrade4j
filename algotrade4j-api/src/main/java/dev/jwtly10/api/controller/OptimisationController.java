package dev.jwtly10.api.controller;

import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.api.service.OptimisationManager;
import dev.jwtly10.core.optimisation.OptimisationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/optimisation")
@Slf4j
public class OptimisationController {
    private final OptimisationManager optimisationManager;

    public OptimisationController(OptimisationManager optimisationManager) {
        this.optimisationManager = optimisationManager;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startOptimisation(@RequestBody StrategyConfig config, @RequestParam("optimisationId") String optimisationId) {
        log.debug("Starting optimisation: {} with config : {}", optimisationId, config);
        optimisationManager.startOptimisation(config, optimisationId);
        return ResponseEntity.ok("Optimisation started");
    }

    @GetMapping("/{optimisationId}/results")
    public ResponseEntity<List<OptimisationResult>> getOptimisationResults(@PathVariable("optimisationId") String optimisationId) {
        log.debug("Getting results for optimisationId: {}", optimisationId);
        List<OptimisationResult> results = optimisationManager.getResults(optimisationId);
        if (results != null) {
            return ResponseEntity.ok(results);
        } else {
            throw new StrategyManagerException("No results found for optimisationId: " + optimisationId, StrategyManagerException.ErrorType.NOT_FOUND);
        }
    }
}