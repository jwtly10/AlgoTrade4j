package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultDataManager implements DataManager, DataProviderListener {
    private final DataProvider dataProvider;
    private final List<DataListener> listeners = new ArrayList<>();
    private final Duration barDuration;
    @Getter
    private final BarSeries barSeries;
    @Getter
    private final String symbol;
    @Getter
    private volatile Number currentBid;
    @Getter
    private Bar currentPartialBar;
    @Getter
    private boolean running = false;
    private ZonedDateTime nextBarCloseTime;
    @Getter
    private volatile Number currentAsk;

    public DefaultDataManager(String symbol, DataProvider dataProvider, Duration barDuration, BarSeries barSeries) {
        this.dataProvider = dataProvider;
        this.barDuration = barDuration;
        this.barSeries = barSeries;
        this.dataProvider.addDataProviderListener(this);
        this.symbol = symbol;
    }

    @Override
    public void start() {
        log.debug("Starting data manager");
        running = true;
        dataProvider.start();
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

        // Update current bid/ask
        this.currentBid = tick.getBid();
        this.currentAsk = tick.getAsk();

        if (currentPartialBar == null) {
            // Initialize the first bar and set the next bar close time
            initializeFirstBar(tick);
        } else if (tick.getDateTime().isEqual(nextBarCloseTime)) {
            // This tick is the close of the current bar.
            currentPartialBar.update(tick);
            log.debug("""
                            Closed A Bar:
                            OpenTime: {},
                            CloseTime: {},
                            Open: {},
                            Close: {}
                            Next Bar Close Time: {}""",
                    currentPartialBar.getOpenTime(), currentPartialBar.getCloseTime(), currentPartialBar.getOpen(), currentPartialBar.getClose(), nextBarCloseTime);
            closeCurrentBarAndCreateNew(tick);
        } else if (tick.getDateTime().isAfter(nextBarCloseTime)) {
            log.warn("We shouldnt generate a tick after the next bar close time");
        } else {
            // Update the current bar
            log.debug("Updating the current bar");
            currentPartialBar.update(tick);
        }

        // Notify listeners of the new tick and current bar
        notifyTick(tick, currentPartialBar);
    }

    private void initializeFirstBar(Tick tick) {
        ZonedDateTime barOpenTime = tick.getDateTime(); // The first tick of a file is always where it starts from
        nextBarCloseTime = barOpenTime.plus(barDuration);
        currentPartialBar = createNewBar(tick, barOpenTime);
        log.debug("""
                        Initialised Bar:
                        OpenTime: {},
                        CloseTime: {},
                        Open: {},
                        Close: {}""",
                currentPartialBar.getOpenTime(), currentPartialBar.getCloseTime(), currentPartialBar.getOpen(), currentPartialBar.getClose());
    }

    private void closeCurrentBarAndCreateNew(Tick tick) {
        // Close the current bar
        barSeries.addBar(currentPartialBar);
        notifyBarClose(currentPartialBar);

        // Create a new bar
        ZonedDateTime newBarOpenTime = nextBarCloseTime;
        nextBarCloseTime = newBarOpenTime.plus(barDuration);
        currentPartialBar = createNewBar(tick, newBarOpenTime);
    }

    private Bar createNewBar(Tick tick, ZonedDateTime openTime) {
        return new DefaultBar(
                tick.getSymbol(),
                barDuration,
                openTime,
                tick.getMid(),
                tick.getMid(),
                tick.getMid(),
                tick.getMid(),
                tick.getVolume()
        );
    }

    private void notifyTick(Tick tick, Bar currentPartialBar) {
        for (DataListener listener : listeners) {
            listener.onTick(tick, currentPartialBar);
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