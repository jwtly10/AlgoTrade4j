package dev.jwtly10.marketdata.dataclients;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.common.ClientCallback;
import dev.jwtly10.marketdata.common.ExternalDataClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
public class OandaDataClient implements ExternalDataClient {
    private static final int MAX_CANDLES_PER_REQUEST = 5000; // Oanda's limit on candles you can request at a time
    private final OandaClient client;

    public OandaDataClient(OandaClient oandaClient) {
        this.client = oandaClient;
    }

    public void fetchCandles(Instrument instrument, ZonedDateTime from, ZonedDateTime to, Duration period, ClientCallback callback) {
        log.debug("Running fetch for Oanda Data client: {} ({}) time: {} -> {}",
                instrument,
                period,
                from.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                to.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        ZonedDateTime currentFrom = from;

        try {
            if (to.isAfter(ZonedDateTime.now())) {
                throw new RuntimeException("Invalid to date - cannot be in the future");
            }

            while (currentFrom.isBefore(to)) {
                ZonedDateTime batchTo = currentFrom.plus(period.multipliedBy(MAX_CANDLES_PER_REQUEST));
                if (batchTo.isAfter(to)) {
                    batchTo = to;
                }

                List<DefaultBar> batchBars = fetchBatch(instrument, currentFrom, batchTo, period);
                log.debug("Found {} bars", batchBars.size());

                if (batchBars.isEmpty()) {
                    break; // No more data available
                }

                for (Bar bar : batchBars) {
                    boolean shouldContinue = callback.onCandle(bar);
                    if (!shouldContinue) {
                        return; // Client requested to stop
                    }
                }

                currentFrom = batchBars.getLast().getOpenTime().plus(period);
            }

            callback.onComplete();
        } catch (Exception e) {
            log.error("Failure while fetching candles and notifying systems. Stopping.", e);
            callback.onError(e);
        }
    }

    private List<DefaultBar> fetchBatch(Instrument instrument, ZonedDateTime from, ZonedDateTime to, Duration period) throws Exception {
        return client.instrumentCandles(instrument)
                .from(from)
                .to(to)
                .granularity(period)
                .fetch();

    }
}