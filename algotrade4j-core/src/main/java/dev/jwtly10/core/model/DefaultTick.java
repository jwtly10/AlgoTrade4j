package dev.jwtly10.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Represents a default implementation of a tick in a trading system.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultTick implements Tick {
    /**
     * The instrument associated with the tick.
     */
    private Instrument instrument;

    /**
     * The bid price of the tick.
     */
    private Number bid;

    /**
     * The mid price of the tick.
     */
    private Number mid;

    /**
     * The ask price of the tick.
     */
    private Number ask;

    /**
     * The volume of the tick.
     */
    private Number volume;

    /**
     * The date and time of the tick.
     */
    private ZonedDateTime dateTime;

    /**
     * Updates the current tick with the provided tick data.
     *
     * @param tick the tick data used to update the current tick
     */
    @Override
    public void update(Tick tick) {
        this.bid = tick.getBid();
        this.mid = tick.getMid();
        this.ask = tick.getAsk();
        this.volume = tick.getVolume();
        this.dateTime = tick.getDateTime();
    }
}