package dev.jwtly10.core;

public interface DataManager {
    void start();

    void addDataListener(DataListener listener);

    Number getCurrentBid();

    Number getCurrentAsk();

    Number getCurrentMidPrice();
}