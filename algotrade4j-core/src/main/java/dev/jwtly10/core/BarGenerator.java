package dev.jwtly10.core;

public interface BarGenerator {
    void addBarListener(BarListener listener);

    void processTick(Tick tick);
}