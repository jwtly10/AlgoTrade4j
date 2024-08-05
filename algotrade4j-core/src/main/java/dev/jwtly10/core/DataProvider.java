package dev.jwtly10.core;

public interface DataProvider {
    void start();

    void addDataProviderListener(DataProviderListener listener);
}