package dev.jwtly10.core.optimisation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimisationRunResult {
    private String strategyId;
    private Map<String, String> parameters;
    private StrategyOutput output;
}