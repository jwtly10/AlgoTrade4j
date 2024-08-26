package dev.jwtly10.core.data;

import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
public class CSVDataProvider implements DataProvider, TickGeneratorCallback {
    @Getter
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ssXXX");
    private final TickGenerator tickGenerator;
    private final String fileName;
    private final int ticksPerBar;
    private final List<DataProviderListener> listeners;
    private final Number spread;
    private final Duration period;
    private final Instrument instrument;
    @Setter
    private DataSpeed dataSpeed = DataSpeed.NORMAL;
    private BufferedReader reader;
    @Getter
    private boolean isRunning;

    public CSVDataProvider(String fileName, int ticksPerBar, Number spread, Duration period, Instrument instrument, long seed) {
        this.fileName = fileName;
        this.ticksPerBar = ticksPerBar;
        this.spread = spread;
        this.period = period;
        this.listeners = new ArrayList<>();
        this.instrument = instrument;
        this.tickGenerator = new TickGenerator(ticksPerBar, Instrument.NAS100USD, spread, period, seed);
    }

    // Overload the constructor to allow creation without a seed
    public CSVDataProvider(String fileName, int ticksPerBar, Number spread, Duration period, Instrument instrument) {
        this(fileName, ticksPerBar, spread, period, instrument, System.currentTimeMillis());
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

            if (isRunning) {
                log.debug("End of file reached. Stopping data feed.");
            }
        } catch (IOException e) {
            log.error("Error reading file", e);
            throw new DataProviderException("Error reading file. Stopping data feed.", e);
        } catch (IllegalStateException e) {
            log.error("The executor was not initialised. The strategy cannot be started.", e);
            throw new DataProviderException("The executor was not initialised. The strategy cannot be started.", e);
        } catch (Exception e) {
            log.error("Error processing bar", e);
            throw new DataProviderException("Unexpected error processing bar. Stopping data feed.", e);
        } finally {
            if (isRunning) {
                log.debug("Data feed stopped");
                stop();
            }
        }
    }

    @Override
    public void stop() {
        if (!isRunning) return;

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

    @Override
    public ZonedDateTime getFrom() {
        // TODO: This class can probably be deprecated now
        return ZonedDateTime.now();
    }

    @Override
    public ZonedDateTime getTo() {
        // TODO: This class can probably be deprecated now
        return ZonedDateTime.now();
    }

    private void processBar(String barData) {
        String[] data = barData.split(",");
        ZonedDateTime dateTime = ZonedDateTime.parse(data[0], dateTimeFormatter);
        Number open = new Number(data[1]);
        Number high = new Number(data[2]);
        Number low = new Number(data[3]);
        Number close = new Number(data[4]);
        Number volume = new Number(data[5]);

        DefaultBar bar = DefaultBar.builder()
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(volume)
                .openTime(dateTime)
                .instrument(instrument)
                .timePeriod(period)
                .build();

        tickGenerator.generateTicks(bar, dataSpeed, this::notifyListeners);
    }

    @Override
    public void onTickGenerated(Tick tick) {
        notifyListeners((DefaultTick) tick);
    }

    private void notifyListeners(DefaultTick tick) {
        for (DataProviderListener listener : listeners) {
            listener.onTick(tick);
        }
    }
}