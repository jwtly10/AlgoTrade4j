package dev.jwtly10.shared.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorType errorType;

    public ApiException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
}