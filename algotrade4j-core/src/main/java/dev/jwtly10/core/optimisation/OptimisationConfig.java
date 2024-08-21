package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class OptimisationConfig {
    private String strategyClass;
    private Instrument instrument;
    private Duration period;
    private Number spread;
    private DataSpeed speed;
    private Number initialCash;
    private List<ParameterRange> parameterRanges;
}