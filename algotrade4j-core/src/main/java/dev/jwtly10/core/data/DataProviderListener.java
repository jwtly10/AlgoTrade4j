package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Tick;

public interface DataProviderListener {

    /**
     * Called when a tick
     *
     * @param tick the new bar
     */
    void onTick(Tick tick);


    /**
     * Called when the provider feed is stopped
     */
    void onStop();
}