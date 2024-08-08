package dev.jwtly10.core.data;

import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class DefaultDataManager implements DataManager, DataProviderListener {
    private final DataProvider dataProvider;
    private final List<DataListener> listeners = new ArrayList<>();
    private final Duration period;
    private final BarSeries barSeries;
    private final String symbol;
    private volatile Number currentBid;
    private volatile Number currentAsk;
    private Bar currentBar;
    private boolean running = false;
    private ZonedDateTime nextBarCloseTime;

    public DefaultDataManager(String symbol, DataProvider dataProvider, Duration barDuration, BarSeries barSeries) {
        this.dataProvider = dataProvider;
        this.period = barDuration;
        this.barSeries = barSeries;
        this.symbol = symbol;
        this.dataProvider.addDataProviderListener(this);
    }

    @Override
    public void start() {
        log.debug("Starting data manager with symbol: {}, period: {}", symbol, period);
        running = true;
        try {
            dataProvider.start();
        } catch (DataProviderException e) {
            log.error("Error starting data provider", e);
            running = false;
        }
    }

    @Override
    public void stop() {
        log.debug("Stopping data manager");
        running = false;
        if (dataProvider.isRunning()) {
            dataProvider.stop();
        }
    }

    @Override
    public void addDataListener(DataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeDataListener(DataListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onTick(Tick tick) {
        if (!running) return;
        log.debug("Received tick: {}", tick);

        updateCurrentPrices(tick);

        if (currentBar == null) {
            log.debug("Initializing new bar");
            initializeNewBar(tick);
            log.debug("New bar initialized: {}", currentBar);
            log.debug("Next bar close time: {}", nextBarCloseTime);
        } else if (tick.getDateTime().isAfter(nextBarCloseTime) || tick.getDateTime().isEqual(nextBarCloseTime)) { // TODO: For now we treat bars closing as -1 second before the next period
            log.debug("Closing current bar because: {} ({})",
                    tick.getDateTime().isAfter(nextBarCloseTime) ? "Tick time is after next bar close time" : "Tick time is equal to next bar close time %s", nextBarCloseTime);
            closeCurrentBar();
            log.debug("Initializing new bar");
            initializeNewBar(tick);
            log.debug("New bar initialized: {}", currentBar);
            log.debug("Next bar close time: {}", nextBarCloseTime);
        } else {
            log.debug("Updating current bar");
            log.debug("Next bar close time: {}", nextBarCloseTime);
            updateBar(tick);
        }

        notifyTick(tick, currentBar);
    }

    private void updateCurrentPrices(Tick tick) {
        this.currentBid = tick.getBid();
        this.currentAsk = tick.getAsk();
    }

    private void initializeNewBar(Tick tick) {
        ZonedDateTime barOpenTime = tick.getDateTime().truncatedTo(ChronoUnit.MINUTES);
        currentBar = new DefaultBar(
                tick.getSymbol(),
                period,
                barOpenTime,
                tick.getMid(),
                tick.getMid(),
                tick.getMid(),
                tick.getMid(),
                tick.getVolume()
        );
        nextBarCloseTime = barOpenTime.plus(period);
        // TODO: Currently we are setting the close time to the next bar close time. This may not be accurate in live trading
        // HOWEVER. It may actually be more representative of the actual close time in live trading
        // To be reviews
        // We did add minus 1 second but this will need to be reviewed
        currentBar.setCloseTime(nextBarCloseTime.minusSeconds(1));

        log.debug("New bar initialized: {}. Next bar close time: {}", currentBar);


    }

    private void updateBar(Tick tick) {
        currentBar.update(tick);
    }

    private void closeCurrentBar() {
        log.debug("Bar being closed: {}", currentBar);
        if (currentBar != null) {
            barSeries.addBar(currentBar);
            notifyBarClose(currentBar);
        }
    }

    private void notifyTick(Tick tick, Bar currentBar) {
        for (DataListener listener : listeners) {
            listener.onTick(tick, currentBar);
        }
    }

    private void notifyBarClose(Bar closedBar) {
        for (DataListener listener : listeners) {
            listener.onBarClose(closedBar);
        }
    }

    @Override
    public void onStop() {
        running = false;
        // When this stops, we trigger the close of the latest bar
        closeCurrentBar();
        for (DataListener listener : listeners) {
            listener.onStop();
        }
    }

    @Override
    public Number getCurrentMidPrice() {
        if (currentBid == null || currentAsk == null) {
            return null;
        }
        return currentBid.add(currentAsk).divide(2);
    }
}