package dev.jwtly10.core;

import dev.jwtly10.core.defaults.DefaultBar;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SimpleBarGenerator implements BarGenerator {
    private final Duration barDuration;
    private final List<BarListener> listeners = new ArrayList<>();
    private Bar currentBar;

    public SimpleBarGenerator(Duration barDuration) {
        this.barDuration = barDuration;
    }

    @Override
    public void addBarListener(BarListener listener) {
        listeners.add(listener);
    }

    @Override
    public void processTick(Tick tick) {
        if (currentBar == null ||
                (tick.getDateTime().isAfter(currentBar.getOpenTime()) &&
                        tick.getDateTime().isBefore(currentBar.getCloseTime()))) {
            // If the current bar is not available (first tick) or the tick is outside the current bar
            if (currentBar != null) {
//                barSeries.addBar(this.currentBar);
//                onBarClose(this.currentBar);
                closeBar();
            }
            // If the current bar is not available, or the tick is outside the current bar, create a new bar
            // This only happens on the first tick, or when the tick is outside the current bar
            currentBar = new DefaultBar(tick.getSymbol(),
                    barDuration, tick.getDateTime(), tick.getMid(), tick.getMid(), tick.getMid(), tick.getMid(),
                    tick.getVolume());
        } else {
            currentBar.update(tick);
        }
    }

    private void closeBar() {
        if (currentBar != null) {
            for (BarListener listener : listeners) {
                listener.onBarClose(currentBar);
            }
        }
    }
}