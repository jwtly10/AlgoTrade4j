package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.model.Bar;

public interface ClientCallback {
    boolean onCandle(Bar bar);

    void onError(Exception exception);

    void onComplete();
}