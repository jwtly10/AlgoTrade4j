package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;

public interface DataListener {
    void onTick(Tick tick, Bar currentBar);

    void onBarClose(Bar bar);

    void onStop();
}