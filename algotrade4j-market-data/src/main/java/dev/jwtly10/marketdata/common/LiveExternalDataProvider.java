package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.data.DataProvider;
import dev.jwtly10.core.data.DataProviderListener;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import dev.jwtly10.marketdata.oanda.response.OandaPriceResponse;
import dev.jwtly10.marketdata.oanda.utils.OandaUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LiveExternalDataProvider implements DataProvider, OandaClient.PriceStreamCallback {
    @Getter
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ssXXX");
    private final Instrument instrument;
    private final List<DataProviderListener> listeners;
    private final OandaBrokerClient oandaClient;
    @Setter
    private DataSpeed dataSpeed = DataSpeed.INSTANT;
    @Getter
    private boolean isRunning;
    private ZonedDateTime from;

    public LiveExternalDataProvider(OandaBrokerClient oandaClient, Instrument instrument) {
        this.oandaClient = oandaClient;
        this.instrument = instrument;
        this.listeners = new ArrayList<>();
    }

    @Override
    public void start() throws DataProviderException {
        if (isRunning) return;
        isRunning = true;
        from = ZonedDateTime.now();

        log.debug("Starting Live Data provider for instrument: {}", instrument);

        try {
            oandaClient.streamPrices(List.of(instrument), this);
        } catch (Exception e) {
            log.error("Error starting live data stream", e);
            throw new DataProviderException("Unexpected error starting live data stream.", e);
        }
    }

    @Override
    public void stop() {
        if (!isRunning) return;

        log.debug("Stopping Live data provider");
        isRunning = false;

        for (DataProviderListener listener : listeners) {
            listener.onStop();
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

    @Override
    public void onPrice(OandaPriceResponse price) {
        if (!isRunning) return;

        try {
            DefaultTick tick = OandaUtils.mapPriceToTick(price);
            log.trace("Received stream tick: {}", tick);
            notifyListeners(tick);
        } catch (Exception e) {
            log.error("Error processing price response", e);
        }
    }

    @Override
    public void onError(Exception e) {
        log.error("Error in price stream", e);
        for (DataProviderListener listener : listeners) {
            listener.onError(new DataProviderException(e.getMessage(), e));
        }
        stop();
    }

    @Override
    public void onComplete() {
        log.debug("Price stream complete");
        stop();
    }
}