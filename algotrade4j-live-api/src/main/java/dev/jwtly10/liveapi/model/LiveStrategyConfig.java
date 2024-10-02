package dev.jwtly10.liveapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.model.InstrumentData;
import dev.jwtly10.core.model.Period;
import dev.jwtly10.core.model.Timeframe;
import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.utils.StrategyReflectionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private List<RunParameter> runParams;
    // These values are not actually needed for a live strategy configuration
    private int spread;
    private DataSpeed speed;
    private Timeframe timeframe;

    public void validate() throws Exception {
        if (strategyClass == null || strategyClass.isEmpty()) {
            throw new IllegalStateException("Strategy class must be specified");
        }
        if (initialCash <= 0) {
            throw new IllegalStateException("Initial cash must be a positive number");
        }
        if (instrumentData == null) {
            throw new IllegalStateException("Instrument data must be specified");
        }

        if (period == null) {
            throw new IllegalStateException("Period must be specified");
        }

        validateRunParams();
    }

    private void validateRunParams() throws Exception {
        Strategy strategy = StrategyReflectionUtils.getStrategyFromClassName(strategyClass, null);
        Map<String, String> runParams = this.runParams.stream().collect(
                Collectors.toMap(RunParameter::getName, RunParameter::getValue));
        ParameterHandler.validateRunParameters(strategy, runParams);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunParameter {
        private String name;
        private String value;
        private String description;
        private String group;
        private String type;
        private List<String> enumValues;
    }
}