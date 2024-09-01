package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.analysis.AnalysisStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyOutput {
    private String strategyId;
    private boolean failed;
    private String reason;
    private AnalysisStats stats;
}