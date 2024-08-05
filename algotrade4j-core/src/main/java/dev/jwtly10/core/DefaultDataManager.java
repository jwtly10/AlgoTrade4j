package dev.jwtly10.core;

import dev.jwtly10.core.defaults.DefaultBar;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DefaultDataManager implements DataManager, DataProviderListener {
    private final DataProvider dataProvider;
    private final List<DataListener> listeners = new ArrayList<>();
    private final Duration barDuration;
    @Getter
    private final BarSeries barSeries;
    private volatile Number currentBid;
    private volatile Number currentAsk;
    @Getter
    private Bar currentPartialBar;
    private boolean running = false;

    public DefaultDataManager(DataProvider dataProvider, Duration barDuration, BarSeries barSeries) {
        this.dataProvider = dataProvider;
        this.barDuration = barDuration;
        this.barSeries = barSeries;
        this.dataProvider.addDataProviderListener(this);
    }

    @Override
    public void start() {
        running = true;
        dataProvider.start();
    }

    @Override
    public void addDataListener(DataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onTick(Tick tick) {
        if (!running) return;

        // Update current bid/ask
        this.currentBid = tick.getBid();
        this.currentAsk = tick.getAsk();

        if (currentPartialBar == null ||
                (tick.getDateTime().isAfter(currentPartialBar.getCloseTime()))) {

            if (currentPartialBar != null) {
                // Close the current bar
                barSeries.addBar(currentPartialBar);
                notifyBarClose(currentPartialBar);
            }

            // Create a new bar
            currentPartialBar = new DefaultBar(
                    tick.getSymbol(),
                    barDuration,
                    tick.getDateTime(),
                    tick.getMid(),
                    tick.getMid(),
                    tick.getMid(),
                    tick.getMid(),
                    tick.getVolume()
            );
        } else {
            // Update the current bar
            currentPartialBar.update(tick);
            // You might want to update indicators here if needed
            // IndicatorUtils.updateIndicators(strategy, currentBar);
        }

        // Notify listeners of the new tick and current bar
        notifyTick(tick, currentPartialBar);
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
    public Number getCurrentBid() {
        return currentBid;
    }

    @Override
    public Number getCurrentAsk() {
        return currentAsk;
    }

    @Override
    public Number getCurrentMidPrice() {
        return currentBid.add(currentAsk).divide(2);
    }
}