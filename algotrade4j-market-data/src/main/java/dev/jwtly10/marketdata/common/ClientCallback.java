package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.model.Bar;

/**
 * ClientCallback Is used to trigger certain events on data, on error and on data complete
 */
public interface ClientCallback {
    /**
     * Trigger logic on new bar data
     *
     * @param bar the bar from the data source
     * @return true is there is candle data/should continue running
     */
    boolean onCandle(Bar bar);

    /**
     * Trigger an on error event
     *
     * @param exception the exception that was thrown by internal logic
     */
    void onError(Exception exception);

    /**
     * Trigger data complete event
     */
    void onComplete();
}