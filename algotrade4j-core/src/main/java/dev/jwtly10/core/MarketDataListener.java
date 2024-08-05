package dev.jwtly10.core;

public interface MarketDataListener {
    void onTick(Tick tick);

    void onBarClose(Bar closedBar);

    void onStop();
}