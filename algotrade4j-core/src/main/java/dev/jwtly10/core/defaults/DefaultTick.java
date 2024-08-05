package dev.jwtly10.core.defaults;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.Tick;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class DefaultTick implements Tick {
    private String symbol;
    private Number bid;
    private Number mid;
    private Number ask;
    private Number volume;
    private ZonedDateTime dateTime;

    @Override
    public void update(Tick tick) {
        this.bid = tick.getBid();
        this.mid = tick.getMid();
        this.ask = tick.getAsk();
        this.volume = tick.getVolume();
        this.dateTime = tick.getDateTime();
    }
}