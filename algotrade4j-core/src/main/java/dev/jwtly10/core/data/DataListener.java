package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;
import lombok.Getter;

public interface DataListener {
    void onTick(Tick tick, Bar currentBar);

    void onBarClose(Bar bar);

    void onStop();

    @Getter
    enum DataFeedSpeed {
        SLOW(3000),    // 3 second per bar
        NORMAL(1000),    // 1 second per bar
        FAST(100),          // 100 milliseconds per bar
        VERY_FAST(10),      // 10 milliseconds per bar
        INSTANT(0);         // No delay

        private final long delayMillis;

        DataFeedSpeed(long delayMillis) {
            this.delayMillis = delayMillis;
        }
    }
}