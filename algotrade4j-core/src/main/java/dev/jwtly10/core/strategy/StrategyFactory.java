package dev.jwtly10.core.strategy;

public interface StrategyFactory {
    Strategy createStrategy(String strategyClass, String id) throws Exception;
}