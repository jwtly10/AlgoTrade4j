package dev.jwtly10.core.exception;

public class InvalidTradeException extends RuntimeException {
    public InvalidTradeException(String message) {
        super(message);
    }

    public InvalidTradeException(String message, Throwable cause) {
        super(message, cause);
    }
}