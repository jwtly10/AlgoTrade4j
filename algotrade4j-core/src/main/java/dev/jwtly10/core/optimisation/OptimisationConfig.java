package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Timeframe;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class OptimisationConfig {
    private String strategyClass;
    private Instrument instrument;
    private Duration period;
    private int spread;
    private DataSpeed speed;
    private double initialCash;
    private List<ParameterRange> parameterRanges;
    private Timeframe timeframe;

    public void validate() throws IllegalStateException {
        if (strategyClass == null || strategyClass.isEmpty()) {
            throw new IllegalStateException("Strategy class must be specified");
        }
        if (instrument == null) {
            throw new IllegalStateException("Instrument must be specified");
        }
        if (period == null || period.isNegative() || period.isZero()) {
            throw new IllegalStateException("Period must be a positive duration");
        }
        if (spread < 0) {
            throw new IllegalStateException("Spread must be a non-negative number");
        }
        if (speed == null) {
            throw new IllegalStateException("Data speed must be specified");
        }
        if (initialCash <= 0) {
            throw new IllegalStateException("Initial cash must be a positive number");
        }
        if (parameterRanges == null || parameterRanges.isEmpty()) {
            throw new IllegalStateException("At least one parameter range must be specified");
        }
        if (timeframe == null) {
            throw new IllegalStateException("Timeframe must be specified");
        }

        // Specifically validate the parameter range
        for (ParameterRange range : parameterRanges) {
            try {
                range.validate();
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid parameter range: " + e.getMessage());
            }
        }
    }
}