package dev.jwtly10.core.exception;

/**
 * Exception thrown when there is an issue with an invalid trade.
 * This could be internal errors in the system or invalid trade data.
 * For broker implementations, errors from APIs when actioning trades should be handled within this.
 */
public class InvalidTradeException extends RuntimeException {

    /**
     * Constructs a new InvalidTradeException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidTradeException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidTradeException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public InvalidTradeException(String message, Throwable cause) {
        super(message, cause);
    }
}