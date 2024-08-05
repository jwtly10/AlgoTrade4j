package dev.jwtly10.core;

public interface DataListener {
    void onTick(Tick tick, Bar currentBar);

    void onBarClose(Bar bar);

    void onStop();
}