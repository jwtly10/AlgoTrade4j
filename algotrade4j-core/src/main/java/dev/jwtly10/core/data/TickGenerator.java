package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Number;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;

@Slf4j
public class TickGenerator {
    private final int ticksPerBar;
    private final Number spread;
    private final Random random;
    private final Duration period;

    public TickGenerator(int ticksPerBar, Number spread, Duration period, long seed) {
        this.ticksPerBar = ticksPerBar;
        this.spread = spread;
        this.period = period;
        this.random = new Random(seed);
    }

    public void generateTicks(Bar bar, DataSpeed speed, TickGeneratorCallback callback) {
        boolean hitLow = false;
        boolean hitHigh = false;
        int lowIndex = -1;
        int highIndex = -1;

        long delayPerTick = speed.delayMillis / ticksPerBar;


        for (int i = 0; i < ticksPerBar; i++) {
            DefaultTick tick = generateSingleTick(bar, i, hitLow, hitHigh, lowIndex, highIndex);
            callback.onTickGenerated(tick);
            if (delayPerTick > 0) {
                try {
                    Thread.sleep(delayPerTick);
                } catch (InterruptedException e) {
                    log.error("Error sleeping during tick generation", e);
                }
            }

            if (tick.getMid().equals(bar.getLow())) hitLow = true;
            if (tick.getMid().equals(bar.getHigh())) hitHigh = true;
        }
    }

    private DefaultTick generateSingleTick(Bar bar, int tickIndex, boolean hitLow, boolean hitHigh, int lowIndex, int highIndex) {
        if (ticksPerBar < 4) {
            throw new IllegalArgumentException("Ticks per bar must be at least 4");
        }

        Number open = bar.getOpen();
        Number high = bar.getHigh();
        Number low = bar.getLow();
        Number close = bar.getClose();
        Number volume = bar.getVolume();

        long nanosDuration = (long) ((double) tickIndex / (ticksPerBar - 1) * period.toNanos());
        ZonedDateTime tickTime = bar.getOpenTime().plus(Duration.ofNanos(nanosDuration));

        log.debug("Tick time is: {}", tickTime);

        Number mid;
        if (ticksPerBar == 4) {
            mid = switch (tickIndex) {
                case 0 -> open;
                case 1 -> high;
                case 2 -> low;
                case 3 -> {
                    tickTime = tickTime.minusSeconds(1);
                    yield close;
                }
                default -> throw new IllegalStateException("Unexpected tickIndex: " + tickIndex);
            };
        } else {
            if (tickIndex == 0) {
                mid = open;
            } else if (tickIndex == ticksPerBar - 1) {
                // This is the last tick
                mid = close;
                // We should always make sure we end a second before next bar
                tickTime = tickTime.minusSeconds(1);
            } else {
                if (lowIndex == -1 || highIndex == -1) {
                    lowIndex = 1 + random.nextInt(ticksPerBar - 3);
                    do {
                        highIndex = 1 + random.nextInt(ticksPerBar - 3);
                    } while (highIndex == lowIndex);
                }

                if (tickIndex == lowIndex || (!hitLow && tickIndex == ticksPerBar - 2)) {
                    mid = low;
                } else if (tickIndex == highIndex || (!hitHigh && tickIndex == ticksPerBar - 2)) {
                    mid = high;
                } else {
                    double randomFactor = random.nextDouble();
                    Number range = high.subtract(low);
                    mid = low.add(range.multiply(BigDecimal.valueOf(randomFactor)));
                }
            }
        }

        Number bid = mid.subtract(spread.divide(2));
        Number ask = mid.add(spread.divide(2));

        Number tickVolume = volume.multiply(BigDecimal.valueOf(random.nextDouble())).divide(BigDecimal.valueOf(ticksPerBar));

        return new DefaultTick(bar.getSymbol(), bid, mid, ask, tickVolume, tickTime);
    }

    public interface TickGeneratorCallback {
        void onTickGenerated(DefaultTick tick);
    }
}