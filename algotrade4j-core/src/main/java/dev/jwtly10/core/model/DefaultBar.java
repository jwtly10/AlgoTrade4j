package dev.jwtly10.core.model;

import lombok.*;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Represents a default implementation of a bar in a trading system.
 */
@Builder
@Getter
@AllArgsConstructor
@ToString
public class DefaultBar implements Bar {
    /**
     * The instrument associated with the bar.
     */
    private Instrument instrument;

    /**
     * The time period of the bar.
     */
    private Duration timePeriod;

    /**
     * The opening price of the bar.
     */
    private Number open;

    /**
     * The opening time of the bar.
     */
    private ZonedDateTime openTime;

    /**
     * The closing time of the bar.
     */
    @Setter
    private ZonedDateTime closeTime;

    /**
     * The highest price of the bar.
     */
    private Number high;

    /**
     * The lowest price of the bar.
     */
    private Number low;

    /**
     * The closing price of the bar.
     */
    private Number close;

    /**
     * The volume of the bar.
     */
    private Number volume;

    /**
     * Constructs a DefaultBar with the specified parameters.
     *
     * @param instrument the instrument associated with the bar
     * @param timePeriod the time period of the bar
     * @param openTime   the opening time of the bar
     * @param open       the opening price of the bar
     * @param high       the highest price of the bar
     * @param low        the lowest price of the bar
     * @param close      the closing price of the bar
     * @param volume     the volume of the bar
     */
    public DefaultBar(Instrument instrument, Duration timePeriod, ZonedDateTime openTime, Number open, Number high, Number low, Number close, Number volume) {
        this.instrument = instrument;
        this.timePeriod = timePeriod;
        this.open = open;
        this.openTime = openTime;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    /**
     * Updates the bar with the provided tick data.
     *
     * @param tick the tick data used to update the bar
     */
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
        // closeTime = tick.getDateTime();
        // TODO: We simulate volume data in backtesting. For live trading this may not exist
        volume = volume.add(tick.getVolume());
    }
}