package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Number;

public interface DataManager {
    void start();

    void stop();

    void addDataListener(DataListener listener);

    void removeDataListener(DataListener listener);

    Number getCurrentBid();

    Number getCurrentAsk();

    Number getCurrentMidPrice();

    String getSymbol();
}