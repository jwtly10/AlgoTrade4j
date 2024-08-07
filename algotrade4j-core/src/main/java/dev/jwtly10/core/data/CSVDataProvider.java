package dev.jwtly10.core.data;

import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Number;
import lombok.Getter;
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
    private BufferedReader reader;
    @Getter
    private boolean isRunning;

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
    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;

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
        ZonedDateTime openTime = ZonedDateTime.parse(data[0], dateTimeFormatter);
        Number open = new Number(data[1]);
        Number high = new Number(data[2]);
        Number low = new Number(data[3]);
        Number close = new Number(data[4]);
        Number volume = new Number(data[5]);

        for (int i = 0; i < ticksPerBar; i++) {
            DefaultTick tick = generateTick(openTime, i, open, high, low, close, volume);
            notifyListeners(tick);
        }
    }

    private DefaultTick generateTick(ZonedDateTime openTime, int tickIndex, Number open, Number high, Number low, Number close, Number volume) {
        double tickProgress = (double) tickIndex / (ticksPerBar - 1);
        long nanosDuration = (long) (period.toNanos() * tickProgress);
        ZonedDateTime tickTime = openTime.plus(Duration.ofNanos(nanosDuration));

        Number mid;
        if (tickIndex == 0) {
            mid = open;
        } else if (tickIndex == ticksPerBar - 1) {
            mid = close;
        } else {
            // Generate a random price within the bar's range
            double randomFactor = random.nextDouble();
            Number range = high.subtract(low);
            mid = low.add(range.multiply(BigDecimal.valueOf(randomFactor)));
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