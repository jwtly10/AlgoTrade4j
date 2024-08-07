package dev.jwtly10.core.data;

public enum DataSpeed {
    SLOW(3000),    // 3 second per bar
    NORMAL(1000),    // 1 second per bar
    FAST(100),          // 100 milliseconds per bar
    VERY_FAST(10),      // 10 milliseconds per bar
    INSTANT(0);         // No delay

    public final long delayMillis;

    DataSpeed(long delayMillis) {
        this.delayMillis = delayMillis;
    }
}