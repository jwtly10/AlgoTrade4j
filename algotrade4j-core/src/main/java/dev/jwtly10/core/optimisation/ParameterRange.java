package dev.jwtly10.core.optimisation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterRange {
    private String name;
    private String start;
    private String end;
    private String step;
}