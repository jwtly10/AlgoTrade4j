package dev.jwtly10.core.defaults;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.Number;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.ZonedDateTime;

@Value
@Builder
public class DefaultBar implements Bar {
    String symbol;
    Duration timePeriod;
    ZonedDateTime dateTime;
    Number open;
    Number high;
    Number low;
    Number close;
    long volume;
}