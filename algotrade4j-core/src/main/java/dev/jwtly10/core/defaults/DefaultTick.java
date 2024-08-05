package dev.jwtly10.core.defaults;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.Tick;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
public class DefaultTick implements Tick {
    String symbol;
    Number price;
    Number volume;
    ZonedDateTime dateTime;
}