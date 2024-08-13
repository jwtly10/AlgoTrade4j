package dev.jwtly10.core.optimisation;

import lombok.Data;

import java.util.List;

@Data
public class OptimisationConfig {
    private String symbol;
    private List<ParameterRange> parameterRanges;
}