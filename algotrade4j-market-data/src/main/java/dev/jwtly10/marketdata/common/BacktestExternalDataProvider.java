package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.data.*;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.exception.RiskException;
import dev.jwtly10.core.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BacktestExternalDataProvider implements DataProvider, TickGeneratorCallback {
    @Getter
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ssXXX");
    private final Instrument instrument;
    private final List<DataProviderListener> listeners;
    private final TickGenerator tickGenerator;
    private final Duration period;
    @Getter
    private final ZonedDateTime from;
    @Getter
    private final ZonedDateTime to;
    private final ExternalDataClient dataClient;
    @Setter
    private DataSpeed dataSpeed = DataSpeed.NORMAL;
    @Getter
    private boolean isRunning;

    public BacktestExternalDataProvider(Broker broker, ExternalDataClient dataClient, Instrument instrument, int spread, Duration period, ZonedDateTime from, ZonedDateTime to, long seed) {
        this.dataClient = dataClient;
        this.instrument = instrument;
        this.period = period;
        this.listeners = new ArrayList<>();
        this.tickGenerator = new TickGenerator(broker, spread, instrument, period, seed);
        this.from = from;
        this.to = to;
    }

    // Overload the constructor to allow creation without a seed for tick generation
    public BacktestExternalDataProvider(Broker broker, ExternalDataClient dataClient, Instrument instrument, int spread, Duration period, ZonedDateTime from, ZonedDateTime to) {
        this(broker, dataClient, instrument, spread, period, from, to, System.currentTimeMillis());
    }

    @Override
    public void start() throws DataProviderException {
        if (isRunning) return;
        isRunning = true;

        log.debug("Starting {} Data provider with period: {}", dataClient.getClass().getName(), period);

        try {
            dataClient.fetchCandles(instrument, from, to, period, new ClientCallback() {
                @Override
                public boolean onCandle(Bar bar) {
                    if (!isRunning) return false;
                    try {
                        tickGenerator.generateTicks(bar, dataSpeed, BacktestExternalDataProvider.this::notifyListeners);
                    } catch (RiskException e) {
                        log.warn("Stopped data provider due to Risk Exception: {}", e.getMessage());
                        return false;
                    } catch (Exception e) {
                        log.warn("Stopping data provider due to unhandled error: {}", e.getMessage(), e);
                        return false;
                    }
                    return true;
                }

                @Override
                public void onError(Exception e) {
                    log.error("Error fetching data: {}", e.getMessage(), e);
                    BacktestExternalDataProvider.this.stop(String.format("Error fetching data: %s", e.getMessage()));
                    for (DataProviderListener listener : listeners) {
                        listener.onError(new DataProviderException(e.getMessage(), e));
                    }
                }

                @Override
                public void onComplete() {
                    log.debug("Data feed complete");
                    BacktestExternalDataProvider.this.stop("Data feed complete");
                }
            });
        } catch (Exception e) {
            log.error("Error processing data: {}", e.getMessage(), e);
            throw new DataProviderException("Unexpected error processing data. Stopping data feed.", e);
        } finally {
            if (isRunning) {
                log.debug("Data feed stopped");
                stop("Data feed stopped");
            }
        }
    }

    @Override
    public void stop(String reason) {
        if (!isRunning) return;

        log.debug("Stopping External data provider: {}", reason);
        isRunning = false;

        for (DataProviderListener listener : listeners) {
            listener.onStop(reason);
        }
    }

    @Override
    public void addDataProviderListener(DataProviderListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(DefaultTick tick) {
        for (DataProviderListener listener : listeners) {
            listener.onTick(tick);
        }
    }

    @Override
    public void onTickGenerated(Tick tick) {
        notifyListeners((DefaultTick) tick);
    }
}