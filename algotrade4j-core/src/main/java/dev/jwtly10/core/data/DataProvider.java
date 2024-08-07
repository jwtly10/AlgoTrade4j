package dev.jwtly10.core.data;

import java.time.format.DateTimeFormatter;

public interface DataProvider {
    DateTimeFormatter getDateTimeFormatter();

    void start();

    void stop();

    boolean isRunning();

    void addDataProviderListener(DataProviderListener listener);
}