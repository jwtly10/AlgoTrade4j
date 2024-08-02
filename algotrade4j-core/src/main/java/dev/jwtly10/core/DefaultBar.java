package dev.jwtly10.core;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDateTime;

@Value
@Builder
public class DefaultBar implements Bar {
    String symbol;
    Duration timePeriod;
    LocalDateTime dateTime;
    Number open;
    Number high;
    Number low;
    Number close;
    long volume;
}