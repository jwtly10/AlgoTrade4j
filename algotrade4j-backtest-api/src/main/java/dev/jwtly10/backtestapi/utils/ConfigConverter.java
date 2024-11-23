package dev.jwtly10.backtestapi.utils;

import dev.jwtly10.backtestapi.model.StrategyConfig;
import dev.jwtly10.core.optimisation.OptimisationConfig;
import dev.jwtly10.core.optimisation.ParameterRange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ConfigConverter {
    public static OptimisationConfig convertToOptimisationConfig(StrategyConfig strategyConfig) {

        log.info("Original Config: {}", strategyConfig);

        OptimisationConfig optimisationConfig = new OptimisationConfig();

        optimisationConfig.setPeriod(strategyConfig.getPeriod().getDuration());
        optimisationConfig.setStrategyClass(strategyConfig.getStrategyClass());
        optimisationConfig.setSpread(strategyConfig.getSpread());
        optimisationConfig.setSpeed(strategyConfig.getSpeed());
        optimisationConfig.setInitialCash(strategyConfig.getInitialCash());
        optimisationConfig.setInstrument(strategyConfig.getInstrumentData().getInstrument());
        optimisationConfig.setTimeframe(strategyConfig.getTimeframe());

        // Convert RunParameters to ParameterRanges
        List<ParameterRange> parameterRanges = strategyConfig.getRunParams().stream()
                .map(ConfigConverter::convertToParameterRange)
                .collect(Collectors.toList());

        optimisationConfig.setParameterRanges(parameterRanges);

        return optimisationConfig;
    }

    private static ParameterRange convertToParameterRange(StrategyConfig.RunParameter runParam) {
        return new ParameterRange(
                runParam.getValue(),
                runParam.getName(),
                runParam.getStart(),
                runParam.getEnd(),
                runParam.getStep(),
                runParam.getSelected(),
                runParam.getStringList()
        );
    }
}