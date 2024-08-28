package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.model.Instrument;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * ExternalDataClient provides an interface for users to implement new integrations with third party providers.
 */
public interface ExternalDataClient {
    /**
     * @param instrument the instrument to fetch data for
     * @param from       where the data should start from
     * @param to         where the data should end
     * @param period     the period of the bar data
     * @param callback   callback for the client to trigger on candle data, on error, and on complete
     */

    void fetchCandles(Instrument instrument, ZonedDateTime from, ZonedDateTime to, Duration period, ClientCallback callback);
}