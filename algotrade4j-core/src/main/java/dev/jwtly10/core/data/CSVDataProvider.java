package dev.jwtly10.core.data;

import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Number;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Default CSV format for importing data.
 * <p>
 * The default CSV format assumes that the CSV file has a header and the fields are in the following order:
 *     <ul>
 *         <li>DateTime</li>
 *         <li>Open</li>
 *         <li>High</li>
 *         <li>Low</li>
 *         <li>Close</li>
 *         <li>Volume</li>
 *     </ul>
 *     The delimiter is a comma.
 *     The time period of the bars is specified in the constructor.
 *     The date time is expected to be in the format "yyyy.MM.dd'T'HH:mm". (UTC assumed)
 *     Example:
 *     <pre>
 *         Date,Open,High,Low,Close,Volume
 *         2022.01.02T22:20:10+00:00,16419.7,16526.0,16310.6,16512.8,209249
 *         2022.01.03T22:20:10+00:00,16516.0,16579.2,16155.8,16276.6,255990
 *         2022.01.04T22:20:10+00:00,16278.4,16283.4,15768.0,15768.6,396027
 *         2022.01.05T22:20:10+00:00,15779.4,15910.6,15614.2,15808.3,629752
 *         2022.01.06T22:20:10+00:00,15826.0,15867.8,15525.2,15586.6,449885
 *    </pre>
 * </p>
 */
@Slf4j
public class CSVDataProvider implements DataProvider {
    @Getter
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ssXXX");
    private final String fileName;
    private final int ticksPerBar;
    private final List<DataProviderListener> listeners;
    private final Random random;
    private final Number spread;
    private final Duration period;
    private final String symbol;
    @Setter
    private DataSpeed dataSpeed = DataSpeed.NORMAL;
    private BufferedReader reader;
    @Getter
    private boolean isRunning;

    private boolean hitLow = false;
    private boolean hitHigh = false;
    private int lowIndex = -1;
    private int highIndex = -1;

    public CSVDataProvider(String fileName, int ticksPerBar, Number spread, Duration period, String symbol, long seed) {
        this.fileName = fileName;
        this.ticksPerBar = ticksPerBar;
        this.spread = spread;
        this.period = period;
        this.listeners = new ArrayList<>();
        this.random = new Random(seed);
        this.symbol = symbol;
    }

    // Overload the constructor to allow creation without a seed
    public CSVDataProvider(String fileName, int ticksPerBar, Number spread, Duration period, String symbol) {
        this(fileName, ticksPerBar, spread, period, symbol, System.currentTimeMillis());
    }

    @Override
    public void start() throws DataProviderException {
        if (isRunning) {
            return;
        }
        isRunning = true;

        log.debug("Starting data provider with period: {}, ticksPerBar: {}", period, ticksPerBar);

        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line;
            // Skip header
            reader.readLine();

            while (isRunning && (line = reader.readLine()) != null) {
                processBar(line);
            }

            log.debug("End of file reached");
        } catch (IOException e) {
            log.error("Error reading file", e);
            throw new DataProviderException("Error reading file. Stopping data feed.", e);
        } catch (Exception e) {
            log.error("Unexpected error", e);
            throw new DataProviderException("Unexpected error. Stopping data feed.", e);
        } finally {
            if (isRunning) {
                stop();
            }
        }
    }

    @Override
    public void stop() {
        log.debug("Stopping data provider");
        isRunning = false;
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            log.error("Error closing file", e);
        }

        for (DataProviderListener listener : listeners) {
            listener.onStop();
        }
    }

    @Override
    public void addDataProviderListener(DataProviderListener listener) {
        listeners.add(listener);
    }

    private void processBar(String barData) {
        String[] data = barData.split(",");
        ZonedDateTime dateTime = ZonedDateTime.parse(data[0], dateTimeFormatter);
        Number open = new Number(data[1]);
        Number high = new Number(data[2]);
        Number low = new Number(data[3]);
        Number close = new Number(data[4]);
        Number volume = new Number(data[5]);

        hitLow = false;
        hitHigh = false;
        lowIndex = -1;
        highIndex = -1;

        long delayPerTick = dataSpeed.delayMillis / ticksPerBar;


        for (int i = 0; i < ticksPerBar; i++) {
            DefaultTick tick = generateTick(dateTime, i, open, high, low, close, volume);
            notifyListeners(tick);
            if (delayPerTick > 0) {
                try {
                    Thread.sleep(delayPerTick);
                } catch (InterruptedException e) {
                    log.error("Error sleeping", e);
                }
            }
        }
    }

    private DefaultTick generateTick(ZonedDateTime dateTime, int tickIndex, Number open, Number high, Number low, Number close, Number volume) {
        if (ticksPerBar < 4) {
            throw new IllegalArgumentException("Ticks per bar must be at least 4");
        }

        long nanosDuration = (long) ((double) tickIndex / (ticksPerBar - 1) * period.toNanos());
        ZonedDateTime tickTime = dateTime.plus(Duration.ofNanos(nanosDuration));

        Number mid;
        if (ticksPerBar == 4) {
            mid = switch (tickIndex) {
                case 0 -> open;
                case 1 -> high;
                case 2 -> low;
                case 3 -> {
                    // We should make sure we always end a second before next bar
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
                // We should make sure we always end a second before next bar
                tickTime = tickTime.minusSeconds(1);
            } else {
                if (lowIndex == -1 || highIndex == -1) {
                    // Initialize indices if not set
                    lowIndex = 1 + random.nextInt(ticksPerBar - 3);
                    do {
                        highIndex = 1 + random.nextInt(ticksPerBar - 3);
                    } while (highIndex == lowIndex);
                }

                if (tickIndex == lowIndex || (!hitLow && tickIndex == ticksPerBar - 2)) {
                    mid = low;
                    hitLow = true;
                } else if (tickIndex == highIndex || (!hitHigh && tickIndex == ticksPerBar - 2)) {
                    mid = high;
                    hitHigh = true;
                } else {
                    // Generate a random price within the bar's range
                    double randomFactor = random.nextDouble();
                    Number range = high.subtract(low);
                    mid = low.add(range.multiply(BigDecimal.valueOf(randomFactor)));
                }
            }
        }

        Number bid = mid.subtract(spread.divide(2));
        Number ask = mid.add(spread.divide(2));

        // Generate a random volume for each tick
        Number tickVolume = volume.multiply(BigDecimal.valueOf(random.nextDouble())).divide(BigDecimal.valueOf(ticksPerBar));

        return new DefaultTick(symbol, bid, mid, ask, tickVolume, tickTime);
    }


    private void notifyListeners(DefaultTick tick) {
        for (DataProviderListener listener : listeners) {
            listener.onTick(tick);
        }
    }
}