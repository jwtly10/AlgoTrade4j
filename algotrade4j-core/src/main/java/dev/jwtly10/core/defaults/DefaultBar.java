package dev.jwtly10.core.defaults;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.Number;
import dev.jwtly10.core.Tick;
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
    private long volume;

    public DefaultBar(String symbol, Duration timePeriod, ZonedDateTime openTime, Number open, Number high, Number low, Number close, long volume) {
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
            open = (tick.getPrice());
            openTime = tick.getDateTime();
        }
        if (high == null || tick.getPrice().compareTo(high) > 0) {
            high = tick.getPrice();
        }
        if (low == null || tick.getPrice().compareTo(low) < 0) {
            low = tick.getPrice();
        }
        close = tick.getPrice();
        closeTime = tick.getDateTime();
        volume += tick.getVolume();
    }
}