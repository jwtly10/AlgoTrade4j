package dev.jwtly10.core.data;

import dev.jwtly10.core.exception.DataProviderException;
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

    /**
     * Called when error with feed
     */
    void onError(DataProviderException e);
}