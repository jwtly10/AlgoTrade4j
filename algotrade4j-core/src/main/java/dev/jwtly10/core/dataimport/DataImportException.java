package dev.jwtly10.core.dataimport;

public class DataImportException extends Exception{
    public DataImportException(String message) {
        super(message);
    }

    public DataImportException(String message, Throwable cause) {
        super(message, cause);
    }
}