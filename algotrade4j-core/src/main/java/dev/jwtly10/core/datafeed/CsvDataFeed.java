package dev.jwtly10.core.datafeed;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.BarDataListener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvDataFeed implements DataFeed {
    private final String filePath;
    private final CsvParseFormat format;
    private final List<BarDataListener> listeners;
    private volatile boolean running;
    private final String symbol;

    // Only used for backtesting testing purposes
    // This should be ignored for api/realtime data feeds
    private final DataFeedSpeed speed;

    public CsvDataFeed(String symbol, String filePath, CsvParseFormat format, DataFeedSpeed speed) {
        this.filePath = filePath;
        this.format = format;
        this.listeners = new ArrayList<>();
        this.speed = speed;
        this.symbol = symbol;
    }

    @Override
    public void addBarDataListener(BarDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void start() throws DataFeedException {
        running = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            if (format.hasHeader()) {
                reader.readLine(); // Skip header
            }
            while ((line = reader.readLine()) != null && running) {
                String[] fields = line.split(format.getDelimiter());
                try {
                    Bar bar = format.parseBar(symbol, fields);
                    for (BarDataListener listener : listeners) {
                        listener.onBar(bar);
                    }
                    if (speed != DataFeedSpeed.INSTANT) {
                        Thread.sleep(speed.getDelayMillis());
                    }
                } catch (Exception e) {
                    throw new DataFeedException("Error parsing line: " + line, e);
                }
            }
        } catch (IOException e) {
            throw new DataFeedException("Error reading file: " + filePath, e);
        }
    }

    @Override
    public void stop() {
        running = false;
    }
}