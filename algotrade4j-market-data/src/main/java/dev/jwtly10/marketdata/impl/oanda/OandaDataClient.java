package dev.jwtly10.marketdata.impl.oanda;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.common.ClientCallback;
import dev.jwtly10.marketdata.common.ExternalDataClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
public class OandaDataClient implements ExternalDataClient {
    private static final int MAX_CANDLES_PER_REQUEST = 4000; // Oanda has a 5000 limit. But this ensures we are always within it
    private final OandaBrokerClient client;

    public OandaDataClient(OandaBrokerClient client) {
        this.client = client;
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

                List<DefaultBar> batchBars = client.fetchCandles(instrument, currentFrom, batchTo, period);
                log.debug("Found {} bars in latest fetch. Processing them now.", batchBars.size());

                if (batchBars.isEmpty()) {
                    break; // No more data available
                }

                int candlesProcessed = 0;
                for (Bar bar : batchBars) {
                    boolean shouldContinue = callback.onCandle(bar);
                    candlesProcessed++;
                    if (candlesProcessed % 300 == 0) {
                        log.debug("{}/{} bars processed", candlesProcessed, batchBars.size());
                    }
                    if (!shouldContinue) {
                        return; // Client requested to stop
                    }
                }

                currentFrom = batchBars.getLast().getOpenTime().plus(period);
            }

            callback.onComplete();
        } catch (Exception e) {
            log.error("Failure while fetching candles and notifying systems. Stopping: {}", e.getMessage(), e);
            callback.onError(e);
        }
    }
}