package dev.jwtly10.marketdata.dataclients;

import dev.jwtly10.core.instruments.OandaInstrument;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.marketdata.common.ExternalDataClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
public class OandaDataClient implements ExternalDataClient {
    private final OandaClient apiClient;

    public OandaDataClient(OandaClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Iterator<Bar> fetchCandlesIterator(String instrument, ZonedDateTime from, ZonedDateTime to, Duration period) {
        return new Iterator<Bar>() {
            private ZonedDateTime currentFrom = from;
            private List<Bar> currentBatch = new ArrayList<>();
            private int currentIndex = 0;
            private boolean isLastBatch = false;

            @Override
            public boolean hasNext() {
                log.debug("hasNext() called. currentIndex: {}, currentBatch.size(): {}, isLastBatch: {}", currentIndex, currentBatch.size(), isLastBatch);
                if (currentIndex < currentBatch.size()) {
                    return true;
                }
                if (isLastBatch) {
                    return false;
                }
                fetchNextBatch();
                return !currentBatch.isEmpty();
            }

            @Override
            public Bar next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Bar bar = currentBatch.get(currentIndex);
                currentIndex++;
                log.debug("Returning bar: {}", bar);
                return bar;
            }

            private void fetchNextBatch() {
                log.debug("fetchNextBatch() called. currentFrom: {}, to: {}", currentFrom, to);

                try {
                    currentBatch = (List<Bar>) (List<?>) apiClient.fetchBars(getInstrumentFromString(instrument), currentFrom, to, period);
                    currentIndex = 0;

                    log.debug("Fetched batch size: {}", currentBatch.size());

                    if (currentBatch.isEmpty()) {
                        isLastBatch = true;
                        log.debug("Empty batch received, setting isLastBatch to true");
                    } else {
                        Bar lastBar = currentBatch.getLast();
                        currentFrom = lastBar.getOpenTime().plus(period);
                        log.debug("Updated currentFrom to: {}", currentFrom);

                        if (currentFrom.isAfter(to) || currentFrom.equals(to)) {
                            isLastBatch = true;
                            log.debug("currentFrom is after or equal to 'to', setting isLastBatch to true");
                        }
                    }
                } catch (Exception e) {
                    // TODO: Handle exceptions, or check if we can use this as a way to tell theres no data left
                    log.error("Error fetching data", e);
                    isLastBatch = true;
                    currentBatch.clear();
                }
            }

            private OandaInstrument getInstrumentFromString(String ins) {
                return switch (ins) {
                    case "NAS100USD" -> OandaInstrument.NAS100_USD;
                    case "SPX500_USD" -> OandaInstrument.SPX500_USD;
                    case "US30_USD" -> OandaInstrument.US30_USD;
                    case "EUR_USD" -> OandaInstrument.EUR_USD;
                    case "GBP_USD" -> OandaInstrument.GBP_USD;
                    case "USD_JPY" -> OandaInstrument.USD_JPY;
                    case "XAU_USD" -> OandaInstrument.XAU_USD;
                    case "BCO_USD" -> OandaInstrument.BCO_USD;
                    default -> throw new IllegalArgumentException("No constant with text " + ins + " found");
                };
            }
        };
    }
}