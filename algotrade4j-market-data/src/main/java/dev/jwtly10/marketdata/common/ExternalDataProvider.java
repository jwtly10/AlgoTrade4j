package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.data.*;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ExternalDataProvider implements DataProvider, TickGeneratorCallback {
    @Getter
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ssXXX");
    private final String instrument;
    private final int ticksPerBar;
    private final List<DataProviderListener> listeners;
    private final TickGenerator tickGenerator;
    private final Duration period;
    private final ZonedDateTime from;
    private final ZonedDateTime to;
    private final ExternalDataClient dataClient;
    @Setter
    private DataSpeed dataSpeed = DataSpeed.NORMAL;
    @Getter
    private boolean isRunning;

    public ExternalDataProvider(ExternalDataClient dataClient, String instrument, int ticksPerBar, Number spread, Duration period, ZonedDateTime from, ZonedDateTime to, long seed) {
        this.dataClient = dataClient;
        this.instrument = instrument;
        this.ticksPerBar = ticksPerBar;
        this.period = period;
        this.listeners = new ArrayList<>();
        this.tickGenerator = new TickGenerator(ticksPerBar, spread, period, seed);
        this.from = from;
        this.to = to;
    }

    // Overload the constructor to allow creation without a seed
    public ExternalDataProvider(ExternalDataClient dataClient, String instrument, int ticksPerBar, Number spread, Duration period, ZonedDateTime from, ZonedDateTime to) {
        this(dataClient, instrument, ticksPerBar, spread, period, from, to, System.currentTimeMillis());
    }

    @Override
    public void start() throws DataProviderException {
        if (isRunning) return;
        isRunning = true;

        log.debug("Starting Oanda data provider with period: {}, ticksPerBar: {}", period, ticksPerBar);

        Iterator<Bar> barIterator = dataClient.fetchCandlesIterator(instrument, from, to, period);

        try {
            while (isRunning && barIterator.hasNext()) {
                Bar bar = barIterator.next();
                tickGenerator.generateTicks(bar, dataSpeed, this::notifyListeners);
            }
        } catch (Exception e) {
            log.error("Error processing data", e);
            throw new DataProviderException("Unexpected error processing data. Stopping data feed.", e);
        } finally {
            if (isRunning) {
                log.debug("Data feed stopped");
                stop();
            }
        }
    }

    @Override
    public void stop() {
        if (!isRunning) return;

        log.debug("Stopping External data provider");
        isRunning = false;

        for (DataProviderListener listener : listeners) {
            listener.onStop();
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