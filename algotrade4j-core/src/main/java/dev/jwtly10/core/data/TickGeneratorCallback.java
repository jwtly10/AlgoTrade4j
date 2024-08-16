package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Tick;

public interface TickGeneratorCallback {
    void onTickGenerated(Tick tick);
}