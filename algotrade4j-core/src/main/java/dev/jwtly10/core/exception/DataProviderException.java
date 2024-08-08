package dev.jwtly10.core.exception;

/**
 * Exception thrown when there is an issue with the data provider.
 */
public class DataProviderException extends Exception {

    /**
     * Constructs a new DataProviderException with the specified detail message.
     *
     * @param message the detail message
     */
    public DataProviderException(String message) {
        super(message);
    }

    /**
     * Constructs a new DataProviderException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public DataProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}