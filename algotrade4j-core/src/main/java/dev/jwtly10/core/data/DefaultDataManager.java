package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
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
    private ZonedDateTime expectedNextBarCloseTime;

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

        updateCurrentPrices(tick);

        if (currentPartialBar == null) {
            initializeFirstBar(tick);
        } else {
            processTickForExistingBar(tick);
        }

        notifyTick(tick, currentPartialBar);
    }

    private void updateCurrentPrices(Tick tick) {
        this.currentBid = tick.getBid();
        this.currentAsk = tick.getAsk();
    }

    private void processTickForExistingBar(Tick tick) {
        ZonedDateTime tickDateTime = tick.getDateTime();

        if (tickDateTime.isAfter(nextBarCloseTime)) {
            handleSkippedPeriod(tick);
        } else if (tickDateTime.isEqual(nextBarCloseTime) || tickDateTime.isAfter(currentPartialBar.getOpenTime().plus(barDuration))) {
            closeCurrentBarAndCreateNew(tick);
        } else {
            currentPartialBar.update(tick);
        }
    }

    private void handleSkippedPeriod(Tick tick) {
        // Close the current bar
        barSeries.addBar(currentPartialBar);
        notifyBarClose(currentPartialBar);

        // Align the new bar's open time with the tick time
        ZonedDateTime newBarOpenTime = alignToBarPeriod(tick.getDateTime());
        nextBarCloseTime = newBarOpenTime.plus(barDuration);

        // Create a new bar starting from the current tick
        currentPartialBar = createNewBar(tick, newBarOpenTime);

        log.info("Skipped period(s). New bar: {}, Next close time: {}", currentPartialBar, nextBarCloseTime);
    }

    private void closeCurrentBarAndCreateNew(Tick tick) {
        barSeries.addBar(currentPartialBar);
        notifyBarClose(currentPartialBar);

        ZonedDateTime newBarOpenTime = nextBarCloseTime;
        nextBarCloseTime = newBarOpenTime.plus(barDuration);
        currentPartialBar = createNewBar(tick, newBarOpenTime);

        log.debug("Closed bar: {}, New bar open time: {}", currentPartialBar, newBarOpenTime);
    }

    private void initializeFirstBar(Tick tick) {
        ZonedDateTime barOpenTime = alignToBarPeriod(tick.getDateTime());
        nextBarCloseTime = barOpenTime.plus(barDuration);
        currentPartialBar = createNewBar(tick, barOpenTime);
        log.debug("Initialized first bar: {}, Next close time: {}", currentPartialBar, nextBarCloseTime);
    }V

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

    private ZonedDateTime alignToBarPeriod(ZonedDateTime dateTime) {
        long epochSeconds = dateTime.toEpochSecond();
        long barSeconds = barDuration.getSeconds();
        long alignedSeconds = (epochSeconds / barSeconds) * barSeconds;
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(alignedSeconds), dateTime.getZone());
    }

//    @Override
//    public void onTick(Tick tick) {
//        if (!running) return;
//        log.debug("Received tick: {}", tick);
//
//        // Update current bid/ask
//        this.currentBid = tick.getBid();
//        this.currentAsk = tick.getAsk();
//
//        if (currentPartialBar == null) {
//            initializeFirstBar(tick);
//        } else if (tick.getDateTime().isAfter(nextBarCloseTime)) {
//            // We've skipped at least one period
//            handleSkippedPeriod(tick);
//        } else if (tick.getDateTime().isEqual(nextBarCloseTime) || tick.getDateTime().isAfter(currentPartialBar.getOpenTime().plus(barDuration))) {
//            // This tick is the close of the current bar or we're at the exact next period.
//            currentPartialBar.update(tick);
//            closeCurrentBarAndCreateNew(tick);
//        } else {
//            // Update the current bar
//            currentPartialBar.update(tick);
//        }
//
//        // Notify listeners of the new tick and current bar
//        notifyTick(tick, currentPartialBar);
//    }
//
//    private void handleSkippedPeriod(Tick tick) {
//        // Close the current bar with the last known data
//        barSeries.addBar(currentPartialBar);
//        notifyBarClose(currentPartialBar);
//
//        // Calculate how many periods we've skipped
//        long periodsSkipped = Duration.between(nextBarCloseTime, tick.getDateTime()).dividedBy(barDuration);
//        log.info("Skipped {} period(s)", periodsSkipped);
//
//        // Adjust the next bar close time to align with the current tick
//        nextBarCloseTime = alignToBarPeriod(tick.getDateTime()).plus(barDuration);
//
//        // Create a new bar starting from the current tick
//        ZonedDateTime newBarOpenTime = nextBarCloseTime.minus(barDuration);
//        currentPartialBar = createNewBar(tick, newBarOpenTime);
//
//        log.debug("Created new bar after skipped period(s): {}, Next close time: {}", currentPartialBar, nextBarCloseTime);
//    }
//
//    private void closeCurrentBarAndCreateNew(Tick tick) {
//        // Close the current bar
//        barSeries.addBar(currentPartialBar);
//        notifyBarClose(currentPartialBar);
//
//        // Create a new bar
//        ZonedDateTime newBarOpenTime = nextBarCloseTime;
//        nextBarCloseTime = newBarOpenTime.plus(barDuration);
//        log.debug("Closed A Bar: {}, Updated next bar close time: {}", currentPartialBar, nextBarCloseTime);
//        currentPartialBar = createNewBar(tick, newBarOpenTime);
//    }
//
//    private ZonedDateTime alignToBarPeriod(ZonedDateTime dateTime) {
//        long epochSeconds = dateTime.toEpochSecond();
//        long barSeconds = barDuration.getSeconds();
//        long alignedSeconds = (epochSeconds / barSeconds) * barSeconds;
//        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(alignedSeconds), dateTime.getZone());
//    }
//
//    private void initializeFirstBar(Tick tick) {
//        ZonedDateTime barOpenTime = alignToBarPeriod(tick.getDateTime());
//        nextBarCloseTime = barOpenTime.plus(barDuration);
//        currentPartialBar = createNewBar(tick, barOpenTime);
//        log.debug("Initialised bar: {}, Next Bar Close Time: {}", currentPartialBar, nextBarCloseTime);
//    }
//
//    private Bar createNewBar(Tick tick, ZonedDateTime openTime) {
//        return new DefaultBar(
//                tick.getSymbol(),
//                barDuration,
//                openTime,
//                tick.getMid(),
//                tick.getMid(),
//                tick.getMid(),
//                tick.getMid(),
//                tick.getVolume()
//        );
//    }

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