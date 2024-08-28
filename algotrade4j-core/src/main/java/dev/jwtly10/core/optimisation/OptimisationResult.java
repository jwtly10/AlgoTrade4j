package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.analysis.AnalysisStats;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OptimisationResult {
    private final List<SuccessfulStrategy> successfulStrategies = new ArrayList<>();
    private List<FailedStrategy> failedStrategies = new ArrayList<>();

    public void addSuccess(SuccessfulStrategy res) {
        successfulStrategies.add(res);
    }

    public void addFail(FailedStrategy res) {
        failedStrategies.add(res);
    }

    public void setFailures(List<FailedStrategy> res) {
        failedStrategies = res;
    }

    public record SuccessfulStrategy(String strategyId, AnalysisStats stats, Map<String, String> parameters) {
    }

    public record FailedStrategy(String strategyId, String failureReason, Map<String, String> parameters) {
    }
}