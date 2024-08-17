package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.analysis.AnalysisStats;
import lombok.Data;

import java.util.Map;

@Data
public class OptimisationResult {
    private final String strategyId;
    private final AnalysisStats stats;
    private final Map<String, String> parameters;

    public OptimisationResult(String strategyId, AnalysisStats stats, Map<String, String> parameters) {
        this.strategyId = strategyId;
        this.stats = stats;
        this.parameters = parameters;
    }
}