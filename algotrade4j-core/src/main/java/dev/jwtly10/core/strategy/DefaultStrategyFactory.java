package dev.jwtly10.core.strategy;

import dev.jwtly10.core.utils.StrategyReflectionUtils;

public class DefaultStrategyFactory implements StrategyFactory {
    @Override
    public Strategy createStrategy(String strategyClass, String id) throws Exception {
        return StrategyReflectionUtils.getStrategyFromClassName(strategyClass, id);
    }
}