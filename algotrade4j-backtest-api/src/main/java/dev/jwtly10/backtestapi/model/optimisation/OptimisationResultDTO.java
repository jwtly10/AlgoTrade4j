package dev.jwtly10.backtestapi.model.optimisation;

import dev.jwtly10.core.optimisation.StrategyOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptimisationResultDTO {
    private Long id;
    private Map<String, String> parameters;
    private StrategyOutput output;
}