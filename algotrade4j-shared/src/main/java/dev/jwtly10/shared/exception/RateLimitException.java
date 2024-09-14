package dev.jwtly10.shared.exception;

public class RateLimitException extends ApiException {
    public RateLimitException(String message) {
        super(message, ErrorType.TOO_MANY_REQUESTS);
    }
}