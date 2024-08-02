package dev.jwtly10.core.datafeed;

public class DataFeedException extends Exception {
    public DataFeedException(String message) {
        super(message);
    }

    public DataFeedException(String message, Throwable cause) {
        super(message, cause);
    }
}