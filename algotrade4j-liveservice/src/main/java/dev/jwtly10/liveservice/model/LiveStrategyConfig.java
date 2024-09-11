package dev.jwtly10.liveservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.model.InstrumentData;
import dev.jwtly10.core.model.Period;
import dev.jwtly10.core.model.Timeframe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class LiveStrategyConfig {
    private String strategyClass;
    private double initialCash;
    private InstrumentData instrumentData;
    private Period period;
    private DataSpeed speed;
    private int spread;
    private Timeframe timeframe;
    private List<RunParameter> runParams;

    public void validate() throws IllegalStateException {
        if (strategyClass == null || strategyClass.isEmpty()) {
            throw new IllegalStateException("Strategy class must be specified");
        }
        if (initialCash <= 0) {
            throw new IllegalStateException("Initial cash must be a positive number");
        }
        if (instrumentData == null) {
            throw new IllegalStateException("Instrument data must be specified");
        }

        if (speed == null) {
            throw new IllegalStateException("Data speed must be specified");
        }
        if (spread < 0) {
            throw new IllegalStateException("Spread must be a non-negative number");
        }
        if (timeframe == null) {
            throw new IllegalStateException("Timeframe must be specified");
        } else {
            validateTimeframe(timeframe);
        }
        if (runParams == null || runParams.isEmpty()) {
            throw new IllegalStateException("At least one run parameter must be specified");
        } else {
            validateRunParameters(runParams);
        }
    }

    private void validateTimeframe(Timeframe timeframe) {
        if (timeframe.getFrom() == null) {
            throw new IllegalStateException("Timeframe 'from' must be specified");
        }
        if (timeframe.getTo() == null) {
            throw new IllegalStateException("Timeframe 'to' must be specified");
        }
        if (timeframe.getFrom().isAfter(timeframe.getTo())) {
            throw new IllegalStateException("Timeframe 'from' must be before 'to'");
        }
    }

    private void validateRunParameters(List<RunParameter> runParams) {
        for (RunParameter param : runParams) {
            if (param.getName() == null || param.getName().isEmpty()) {
                throw new IllegalStateException("Run parameter name must be specified");
            }
            if (param.getValue() == null || param.getValue().isEmpty()) {
                throw new IllegalStateException("Run parameter value must be specified");
            }
        }
    }

    @Data
    @Builder
    public static class RunParameter {
        private String name;
        private String value;
        private String description;
        private String group;
    }
}