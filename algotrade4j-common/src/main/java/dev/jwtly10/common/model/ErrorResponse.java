package dev.jwtly10.common.model;

import dev.jwtly10.common.exception.ErrorType;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String message;
    private final ErrorType errorType;

    public ErrorResponse(String message, ErrorType errorType) {
        this.message = message;
        this.errorType = errorType;
    }

}