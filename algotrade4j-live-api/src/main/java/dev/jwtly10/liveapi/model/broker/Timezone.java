package dev.jwtly10.liveapi.model.broker;

import lombok.Getter;

import java.time.ZoneId;

@Getter
public enum Timezone {
    UTC("UTC"),
    NY("America/New_York"),
    LONDON("Europe/London"),
    TOKYO("Asia/Tokyo"),
    SYDNEY("Australia/Sydney"),
    MOSCOW("Europe/Moscow"),
    DUBAI("Asia/Dubai");

    private final String zoneId;

    Timezone(String zoneId) {
        this.zoneId = zoneId;
    }

    public ZoneId getZoneId() {
        return ZoneId.of(zoneId);
    }
}