package dev.jwtly10.core.model;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum Period {
    M1(Duration.ofMinutes(1)),
    M5(Duration.ofMinutes(5)),
    M15(Duration.ofMinutes(15)),
    M30(Duration.ofMinutes(30)),
    H1(Duration.ofHours(1)),
    H4(Duration.ofHours(4)),
    D(Duration.ofDays(1));

    private final Duration duration;

    Period(Duration duration) {
        this.duration = duration;
    }
}