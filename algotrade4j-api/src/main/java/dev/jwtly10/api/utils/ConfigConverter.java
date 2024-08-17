package dev.jwtly10.api.utils;

import dev.jwtly10.api.exception.ErrorType;
import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.core.optimisation.OptimisationConfig;
import dev.jwtly10.core.optimisation.ParameterRange;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigConverter {
    public static OptimisationConfig convertToOptimisationConfig(StrategyConfig strategyConfig) {
        OptimisationConfig optimisationConfig = new OptimisationConfig();

        Duration period = switch (strategyConfig.getPeriod()) {
            // TODO: Support other times
//            case "1m" -> Duration.ofMinutes(1);
//            case "5m" -> Duration.ofMinutes(5);
//            case "15m" -> Duration.ofMinutes(15);
//            case "1H" -> Duration.ofHours(1);
//            case "4H" -> Duration.ofHours(4);
            case "1D" -> Duration.ofDays(1);
            default -> throw new StrategyManagerException("Invalid duration: " + strategyConfig.getPeriod(), ErrorType.BAD_REQUEST);
        };
        optimisationConfig.setPeriod(period);
        optimisationConfig.setStrategyClass(strategyConfig.getStrategyClass());
        optimisationConfig.setSpread(strategyConfig.getSpread());
        optimisationConfig.setSpeed(strategyConfig.getSpeed());
        optimisationConfig.setInitialCash(strategyConfig.getInitialCash());
        optimisationConfig.setSymbol(strategyConfig.getSymbol());

        // Convert RunParameters to ParameterRanges
        List<ParameterRange> parameterRanges = strategyConfig.getRunParams().stream()
                .filter(param -> param.getSelected() != null && param.getSelected())
                .map(ConfigConverter::convertToParameterRange)
                .collect(Collectors.toList());

        optimisationConfig.setParameterRanges(parameterRanges);

        return optimisationConfig;
    }

    private static ParameterRange convertToParameterRange(StrategyConfig.RunParameter runParam) {
        return new ParameterRange(
                runParam.getName(),
                runParam.getStart(),
                runParam.getStop(),
                runParam.getStep()
        );
    }
}