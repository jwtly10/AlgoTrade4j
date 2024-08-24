package dev.jwtly10.core.exception;

/**
 * Exception thrown when there is an risk rules exceeded
 * This could be internal errors in the system or invalid trade data.
 * For broker implementations, errors from APIs when actioning trades should be handled within this.
 */
public class RiskException extends RuntimeException {

    /**
     * Constructs a new RiskExceededException with the specified detail message.
     *
     * @param message the detail message
     */
    public RiskException(String message) {
        super(message);
    }

    /**
     * Constructs a new RiskException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public RiskException(String message, Throwable cause) {
        super(message, cause);
    }
}