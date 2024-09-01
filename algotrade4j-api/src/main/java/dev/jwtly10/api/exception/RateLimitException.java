package dev.jwtly10.api.exception;

public class RateLimitException extends ApiException {
    public RateLimitException(String message) {
        super(message, ErrorType.TOO_MANY_REQUESTS);
    }
}