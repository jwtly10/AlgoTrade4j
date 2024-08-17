package dev.jwtly10.api.models;

import dev.jwtly10.api.exception.StrategyManagerException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private StrategyManagerException.ErrorType errorType;
}