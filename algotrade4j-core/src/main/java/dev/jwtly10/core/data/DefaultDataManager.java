package dev.jwtly10.core.data;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.LogEvent;
import dev.jwtly10.core.exception.BacktestExecutorException;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DefaultDataManager implements DataManager, DataProviderListener {
    private final DataProvider dataProvider;
    private final List<DataListener> listeners = new CopyOnWriteArrayList<>();
    private final Duration period;
    @Getter
    private final BarSeries barSeries;
    @Getter
    private final Instrument instrument;
    private final EventPublisher eventPublisher;
    private final String runId;
    @Getter
    private volatile Number currentBid;
    @Getter
    private volatile Number currentAsk;
    @Getter // For testing
    @Setter
    private Bar currentBar;
    @Getter // For testing
    @Setter
    private ZonedDateTime nextBarCloseTime;
    @Getter
    private boolean running = false;
    private ZonedDateTime currentDay;

    // Meta data
    private Instant startTime;
    @Getter
    private int ticksModeled;
    private boolean isOptimising = false;

    public DefaultDataManager(String runId, Instrument instrument, DataProvider dataProvider, Duration barDuration, BarSeries barSeries, EventPublisher eventPublisher) {
        this.runId = runId;
        this.dataProvider = dataProvider;
        this.period = barDuration;
        this.barSeries = barSeries;
        this.instrument = instrument;
        this.dataProvider.addDataProviderListener(this);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void initialise(Bar currentBar, ZonedDateTime nextBarCloseTime) {
        this.currentBar = currentBar;
        this.nextBarCloseTime = nextBarCloseTime;
    }

    @Override
    public void start() {
        log.info("Starting data manager with instrument: {}, period: {}", instrument, period);
        running = true;
        startTime = Instant.now();
        try {
            // TODO: ThIs should only be happening in the live trading variant


            dataProvider.start();
        } catch (DataProviderException e) {
            log.error("Error starting data provider", e);
            running = false;
            startTime = null;
            eventPublisher.publishErrorEvent(runId, e);
        }
    }

    @Override
    public void stop() {
        if (!running) return;

        running = false;

        // Stop the provider if its running
        if (dataProvider.isRunning()) {
            dataProvider.stop();
        }

        Duration runningTime = Duration.between(startTime, Instant.now());
        log.info("Data Manager stopped. Runtime: {}, ticks modelled: {}", formatDuration(runningTime), ticksModeled);

        // Stop data listeners (AKA strategies)
        notifyStop();
    }

    @Override
    public void onError(DataProviderException e) {
        // Here we just emit the event. The strategy will have stopped anyway so at least we can show errors if needed
        eventPublisher.publishErrorEvent(runId, e);
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
        if (listeners.isEmpty()) {
            // There are no more listeners here
            log.warn("No more listeners. Stopping data manager.");
            stop();
        }

        log.trace("Received tick: {}", tick);
        ticksModeled++;

        try {
            updateCurrentPrices(tick);
            checkNewDay(tick.getDateTime());

            if (currentBar == null) {
                initializeNewBar(tick);
            } else if (tick.getDateTime().isAfter(nextBarCloseTime) || tick.getDateTime().isEqual(nextBarCloseTime)) { // TODO: For now we treat bars closing as -1 second before the next period
                log.trace("Closing current bar because: {} ({})",
                        tick.getDateTime().isAfter(nextBarCloseTime) ? "Tick time is after next bar close time" : "Tick time is equal to next bar close time %s", nextBarCloseTime);
                closeCurrentBar();
                initializeNewBar(tick);
            } else {
                updateBar(tick);
            }

            notifyTick(tick, currentBar);
        } catch (BacktestExecutorException e) {
            // During a strategy run, here is where we handle any errors that may happen
            log.error("Error during strategy run for strategy {}: ", e.getStrategyId(), e);
            eventPublisher.publishEvent(new LogEvent(e.getStrategyId(), LogEvent.LogType.ERROR, "Error during strategy run: %s", e.getMessage()));
            eventPublisher.publishErrorEvent(e.getStrategyId(), e);
            // If we are optimising. Gracefully remove the listener, rather than stopping the data manager
            if (isOptimising) {
                notifyStopForStrategyId(e.getStrategyId());
            } else {
                stop();
            }
        }
    }

    private void checkNewDay(ZonedDateTime tickDateTime) {
        ZonedDateTime tickDay = tickDateTime.truncatedTo(ChronoUnit.DAYS);
        if (currentDay == null || !tickDay.isEqual(currentDay)) {
            currentDay = tickDay;
            notifyNewDay(currentDay);
        }
    }

    private void updateCurrentPrices(Tick tick) {
        this.currentBid = tick.getBid();
        this.currentAsk = tick.getAsk();
    }

    private void initializeNewBar(Tick tick) {
        ZonedDateTime barOpenTime = tick.getDateTime().truncatedTo(ChronoUnit.MINUTES);
        currentBar = new DefaultBar(
                tick.getInstrument(),
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
    }

    private void updateBar(Tick tick) {
        currentBar.update(tick);
    }

    private void closeCurrentBar() {
        log.trace("Bar being closed: {}", currentBar);
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

    private void notifyStop() {
        for (DataListener listener : listeners) {
            listener.onStop();
        }
    }

    private void notifyStopForStrategyId(String strategyId) {
        for (DataListener listener : listeners) {
            if (listener.getStrategyId().equals(strategyId)) {
                removeDataListener(listener);
            }
        }
    }

    private void notifyNewDay(ZonedDateTime newDay) {
        for (DataListener listener : listeners) {
            listener.onNewDay(newDay);
        }
    }

    private String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        StringBuilder formattedDuration = new StringBuilder();
        if (minutes > 0) {
            formattedDuration.append(minutes).append(minutes == 1 ? " min " : " mins ");
        }
        if (seconds > 0 || minutes == 0) {
            formattedDuration.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }
        return formattedDuration.toString().trim();
    }

    /*
     * Called when the provider feed is stopped
     * This should stop the data manager, and notify all listeners
     */
    @Override
    public void onStop() {
        if (running) {
            log.debug("Data provider stopped. Stopping data manager");
            // When this stops, we trigger the close of the latest bar
            closeCurrentBar();
            stop();
        }
    }

    @Override
    public Number getCurrentMidPrice() {
        if (currentBid == null || currentAsk == null) {
            return null;
        }
        return currentBid.add(currentAsk).divide(2);
    }

    @Override
    public ZonedDateTime getFrom() {
        return dataProvider.getFrom();
    }

    @Override
    public ZonedDateTime getTo() {
        return dataProvider.getTo();
    }

    @Override
    public void setIsOptimising(boolean value) {
        this.isOptimising = true;
    }

}