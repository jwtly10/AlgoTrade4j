package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;

@Slf4j
public class TickGenerator {
    private final int ticksPerBar;
    private final Number spread;
    private final Random random;
    private final Duration period;
    private final Instrument instrument;
    private Number remainingVolume; // Used for ensuring that the total volume is maintained

    public TickGenerator(int ticksPerBar, Instrument instrument, Number spread, Duration period, long seed) {
        if (ticksPerBar < 4) {
            throw new IllegalArgumentException("Ticks per bar must be at least 4");
        }
        this.ticksPerBar = ticksPerBar;
        this.spread = spread;
        this.period = period;
        this.random = new Random(seed);
        this.instrument = instrument;
    }

    public TickGenerator(Number spread, Instrument instrument, Duration period, long seed) {
        this.ticksPerBar = mapTicksPerBarToPeriod(period);
        log.info("For duration {}, generating {} ticks", period, ticksPerBar);
        this.spread = spread;
        this.period = period;
        this.random = new Random(seed);
        this.instrument = instrument;
    }

    /**
     * We should just let the system generate the ticks based on the timeframe
     * this way we have a consistent way to test these strategies (paid with a single seeded random)
     *
     * @param period the period of the strategy run
     * @return ticks to return per bar
     */
    private int mapTicksPerBarToPeriod(Duration period) {
        return switch (period) {
            case Duration d when d.equals(Duration.ofMinutes(1)) -> 20;
            case Duration d when d.equals(Duration.ofMinutes(5)) -> 40;
            case Duration d when d.equals(Duration.ofMinutes(15)) -> 60;
            case Duration d when d.equals(Duration.ofMinutes(30)) -> 80;
            case Duration d when d.equals(Duration.ofHours(1)) -> 100;
            case Duration d when d.equals(Duration.ofHours(4)) -> 150;
            case Duration d when d.equals(Duration.ofDays(1)) -> 200;
            default -> throw new IllegalArgumentException("Unexpected period: " + period);
        };
    }

    /**
     * Generates ticks for a given bar and invokes the callback for each generated tick.
     * Ensures that the number of ticks per bar is at least 4.
     * Randomly determines the indices for the low and high ticks if ticksPerBar is greater than 4.
     * Calculates the delay per tick based on the data speed.
     * Generates each tick and invokes the callback.
     * Sleeps for the calculated delay per tick if delay is greater than 0.
     *
     * @param bar      the bar for which ticks are generated
     * @param speed    the speed at which data is generated
     * @param callback the callback to be invoked for each generated tick
     * @throws IllegalArgumentException if ticksPerBar is less than 4
     */
    public void generateTicks(Bar bar, DataSpeed speed, TickGeneratorCallback callback) {
        if (ticksPerBar < 4) {
            throw new IllegalArgumentException("Ticks per bar must be at least 4");
        }
        remainingVolume = bar.getVolume();
        boolean hitLow = false;
        boolean hitHigh = false;
        int lowIndex = -1;
        int highIndex = -1;

        if (ticksPerBar > 4) {
            lowIndex = 1 + random.nextInt(ticksPerBar - 3);
            do {
                highIndex = 1 + random.nextInt(ticksPerBar - 3);
            } while (highIndex == lowIndex);
        }

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
        Number open = bar.getOpen();
        Number high = bar.getHigh();
        Number low = bar.getLow();
        Number close = bar.getClose();
        Number volume = bar.getVolume();

        long nanosDuration = (long) ((double) tickIndex / (ticksPerBar - 1) * period.toNanos());
        ZonedDateTime tickTime = bar.getOpenTime().plus(Duration.ofNanos(nanosDuration));

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
            // If more than 4 ticks, generate a random price within the bar's range
            // But ensure that the first and last ticks are the open and close prices
            // and at some point, the high and low prices are represented
            if (tickIndex == 0) {
                mid = open;
            } else if (tickIndex == ticksPerBar - 1) {
                // This is the last tick
                mid = close;
                // We should always make sure we end a second before next bar
                tickTime = tickTime.minusSeconds(1);
            } else {
                // Ensuring that the high and low prices are represented
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

        Number calculatedSpread = new Number(this.spread.doubleValue() * Math.pow(10, -instrument.getDecimalPlaces()));

        Number bid = mid.subtract(calculatedSpread.divide(2));
        Number ask = mid.add(calculatedSpread.divide(2));

        Number tickVolume;
        if (tickIndex == ticksPerBar - 1) {
            // Last tick gets all remaining volume
            tickVolume = remainingVolume;
        } else {
            // Distribute volume randomly, but ensure we don't exceed remaining volume
            double randomFactor = random.nextDouble() * (2.0 / ticksPerBar);
            tickVolume = remainingVolume.multiply(BigDecimal.valueOf(randomFactor));
            if (tickVolume.compareTo(remainingVolume) > 0) {
                tickVolume = remainingVolume;
            }
        }
        remainingVolume = remainingVolume.subtract(tickVolume);

        return new DefaultTick(
                bar.getInstrument(),
                bid.setScale(bar.getInstrument().getDecimalPlaces(), RoundingMode.DOWN),
                mid.setScale(bar.getInstrument().getDecimalPlaces(), RoundingMode.DOWN),
                ask.setScale(bar.getInstrument().getDecimalPlaces(), RoundingMode.DOWN),
                tickVolume.setScale(bar.getInstrument().getDecimalPlaces(), RoundingMode.DOWN),
                tickTime);
    }

    public interface TickGeneratorCallback {
        void onTickGenerated(DefaultTick tick);
    }
}