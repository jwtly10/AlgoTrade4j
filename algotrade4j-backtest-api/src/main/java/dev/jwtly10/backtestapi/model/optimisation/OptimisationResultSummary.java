package dev.jwtly10.backtestapi.model.optimisation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptimisationResultSummary {
    private Long totalCombinations;
    private Long successfulRuns;
    private Long failedRuns;
}