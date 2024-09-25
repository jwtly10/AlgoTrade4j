package dev.jwtly10.backtestapi.exception;

import dev.jwtly10.shared.exception.ErrorType;
import lombok.Getter;

@Getter
public class StrategyManagerException extends RuntimeException {
    private final ErrorType errorType;

    public StrategyManagerException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
}