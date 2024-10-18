package dev.jwtly10.core.exception;

import dev.jwtly10.core.strategy.Strategy;
import lombok.Getter;

/**
 * A wrapper for strategy backtest specific exception, providing context about the exact strategy
 * that failed, so that it can be handled gracefully
 */
@Getter
public class BacktestExecutorException extends RuntimeException {
    private final Strategy strategy;

    public BacktestExecutorException(Strategy strategyId, String message) {
        super(message);
        this.strategy = strategyId;
    }

    public BacktestExecutorException(Strategy strategy, String message, Throwable cause) {
        super(message, cause);
        this.strategy = strategy;
    }


    public String getStrategyId() {
        return strategy.getStrategyId();
    }
}