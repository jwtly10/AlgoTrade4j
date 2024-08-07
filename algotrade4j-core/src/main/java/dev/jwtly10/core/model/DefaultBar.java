package dev.jwtly10.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.ZonedDateTime;

@Builder
@Getter
@AllArgsConstructor
public class DefaultBar implements Bar {
    private String symbol;
    private Duration timePeriod;
    private Number open;
    private ZonedDateTime openTime;
    private ZonedDateTime closeTime;
    private Number high;
    private Number low;
    private Number close;
    private Number volume;

    public DefaultBar(String symbol, Duration timePeriod, ZonedDateTime openTime, Number open, Number high, Number low, Number close, Number volume) {
        this.symbol = symbol;
        this.timePeriod = timePeriod;
        this.open = open;
        this.openTime = openTime;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public void update(Tick tick) {
        if (open == null) {
            open = (tick.getMid());
            openTime = tick.getDateTime();
        }
        if (high == null || tick.getMid().compareTo(high) > 0) {
            high = tick.getMid();
        }
        if (low == null || tick.getMid().compareTo(low) < 0) {
            low = tick.getMid();
        }
        close = tick.getMid();
        closeTime = tick.getDateTime();
        // TODO: We simulate volume data in backtesting. For live trading this may not exist
        volume = volume.add(tick.getVolume());
    }
}