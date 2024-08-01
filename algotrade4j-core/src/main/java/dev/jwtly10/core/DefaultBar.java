package dev.jwtly10.core;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDateTime;

@Value
@Builder
public class DefaultBar implements Bar {
    Duration timePeriod;
    LocalDateTime dateTime;
    Price open;
    Price high;
    Price low;
    Price close;
    long volume;
}