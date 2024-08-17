package dev.jwtly10.api.service;

import dev.jwtly10.api.exception.ErrorType;
import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.api.utils.ConfigConverter;
import dev.jwtly10.core.optimisation.OptimisationConfig;
import dev.jwtly10.core.optimisation.OptimisationExecutor;
import dev.jwtly10.core.optimisation.OptimisationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OptimisationManager {
    private final OptimisationExecutor optimisationExecutor;
    private final Map<String, List<OptimisationResult>> optimisationResults = new ConcurrentHashMap<>();


    public OptimisationManager(OptimisationExecutor optimisationExecutor) {
        this.optimisationExecutor = optimisationExecutor;
    }

    public void startOptimisation(StrategyConfig config, String optimisationId) {
        OptimisationConfig optimisationConfig = ConfigConverter.convertToOptimisationConfig(config);

        CompletableFuture.runAsync(() -> {
            try {
                List<OptimisationResult> results = optimisationExecutor.runOptimisation(optimisationConfig);
                optimisationResults.put(optimisationId, results);
            } catch (Exception e) {
                log.error("Failed to start optimisation", e);
                throw new StrategyManagerException("Failed to start optimisation: " + e.getMessage(), ErrorType.BAD_REQUEST);
            }
        });
    }

    public List<OptimisationResult> getResults(String optimisationId) {
        log.debug("All optimisation results: {}", optimisationResults);
        return optimisationResults.get(optimisationId);
    }
}