package dev.jwtly10.core.optimisation;

import lombok.Data;

@Data
public class ParameterRange {
    private String name;
    private String start;
    private String end;
    private String step;
}