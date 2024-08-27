package dev.jwtly10.core.exception;

import lombok.Getter;

/**
 * A wrapper for strategy backtest specific exception, providing context about the exact strategy
 * that failed, so that it can be handled gracefully
 */
@Getter
public class BacktestExecutorException extends RuntimeException {
    private final String strategyId;

    public BacktestExecutorException(String strategyId, String message) {
        super(message);
        this.strategyId = strategyId;
    }

    public BacktestExecutorException(String errorId, String message, Throwable cause) {
        super(message, cause);
        this.strategyId = errorId;
    }

    public String getErrorId() {
        return strategyId;
    }
}