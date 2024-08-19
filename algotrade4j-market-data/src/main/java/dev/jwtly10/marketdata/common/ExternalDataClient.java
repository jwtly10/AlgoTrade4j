package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.model.Instrument;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface ExternalDataClient {
    void fetchCandles(Instrument instrument, ZonedDateTime from, ZonedDateTime to, Duration period, ClientCallback callback);
}