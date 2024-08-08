package dev.jwtly10.core.data;

import dev.jwtly10.core.exception.DataProviderException;

import java.time.format.DateTimeFormatter;

public interface DataProvider {
    DateTimeFormatter getDateTimeFormatter();

    void start() throws DataProviderException;

    void stop();

    boolean isRunning();

    void addDataProviderListener(DataProviderListener listener);
}