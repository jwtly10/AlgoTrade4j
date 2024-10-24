package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.data.DataProvider;
import dev.jwtly10.core.data.DataProviderListener;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.marketdata.common.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Interfaces with an external data provider to provide live data.
 * And trigger all listeners with each tick.
 */
@Slf4j
public class LiveExternalDataProvider implements DataProvider {
    @Getter
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ssXXX");
    private final Instrument instrument;
    private final List<DataProviderListener> listeners;
    private final BrokerClient client;
    @Setter
    private DataSpeed dataSpeed = DataSpeed.INSTANT;
    @Getter
    private boolean isRunning;
    private ZonedDateTime from;

    private Stream<Tick> priceStream;

    public LiveExternalDataProvider(BrokerClient client, Instrument instrument) {
        this.client = client;
        this.instrument = instrument;
        this.listeners = new ArrayList<>();
    }

    @Override
    public void start() throws DataProviderException {
        if (isRunning) return;
        isRunning = true;
        from = ZonedDateTime.now();

        log.debug("Starting Live Data provider for instrument: {}", instrument);
        this.priceStream = (Stream<Tick>) client.streamPrices(List.of(instrument));
        priceStream.start(new Stream.StreamCallback<>() {
            @Override
            public void onData(Tick price) {
                onPrice(price);
            }

            @Override
            public void onError(Exception e) {
                onPriceStreamError(e);
            }

            @Override
            public void onComplete() {
                onPriceStreamComplete();
            }
        });
    }

    @Override
    public void stop(String reason) {
        if (!isRunning) return;

        log.debug("Stopping Live data provider: {}", reason);
        isRunning = false;

        if (priceStream != null) {
            priceStream.close();
        }

        for (DataProviderListener listener : listeners) {
            listener.onStop(reason);
        }
    }

    @Override
    public void addDataProviderListener(DataProviderListener listener) {
        listeners.add(listener);
    }

    @Override
    public ZonedDateTime getFrom() {
        return from;
    }

    @Override
    public ZonedDateTime getTo() {
        return ZonedDateTime.now();
    }

    private void notifyListeners(DefaultTick tick) {
        for (DataProviderListener listener : listeners) {
            listener.onTick(tick);
        }
    }

    public void onPrice(Tick tick) {
        if (!isRunning) return;

        try {
            log.trace("Received stream tick: {}", tick);
            notifyListeners((DefaultTick) tick);
        } catch (Exception e) {
            log.error("Error processing price response: {}", e.getMessage(), e);
        }
    }

    public void onPriceStreamError(Exception e) {
        log.error("Error in price stream: {}. Triggering shutdown", e.getMessage(), e);
        for (DataProviderListener listener : listeners) {
            listener.onError(new DataProviderException(e.getMessage(), e));
        }
        stop(String.format("Error in price stream: %s", e.getMessage()));
    }

    public void onPriceStreamComplete() {
        log.debug("Price stream complete");
        stop("Price stream complete");
    }
}