package dev.jwtly10.core.datafeed;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvDataFeedTest {

    @TempDir
    Path tempDir;

    private File csvFile;
    private CsvFormat format;

    @BeforeEach
    void setUp() throws IOException {
        csvFile = tempDir.resolve("test.csv").toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("DateTime,Open,High,Low,Close,Volume\n");
            writer.write("2022.01.02T22:00,16419.7,16526.0,16310.6,16512.8,209249\n");
            writer.write("2022.01.03T22:00,16516.0,16579.2,16155.8,16276.6,255990\n");
        }

        format = new DefaultCsvFormat(Duration.ofDays(1));
    }

    @Test
    void testCsvDataFeed() throws DataFeedException {
        CsvDataFeed dataFeed = new CsvDataFeed(csvFile.getAbsolutePath(), format, DataFeedSpeed.INSTANT);
        List<Bar> receivedBars = new ArrayList<>();

        dataFeed.addBarDataListener(receivedBars::add);

        dataFeed.start();

        assertEquals(2, receivedBars.size(), "Should have received 2 bars");

        Bar firstBar = receivedBars.getFirst();
        assertEquals(LocalDateTime.parse("2022-01-02T22:00"), firstBar.getDateTime());
        assertEquals(new Price("16419.7"), firstBar.getOpen());
        assertEquals(new Price("16526.0"), firstBar.getHigh());
        assertEquals(new Price("16310.6"), firstBar.getLow());
        assertEquals(new Price("16512.8"), firstBar.getClose());
        assertEquals(209249, firstBar.getVolume());

        Bar secondBar = receivedBars.get(1);
        assertEquals(LocalDateTime.parse("2022-01-03T22:00"), secondBar.getDateTime());
        assertEquals(new Price("16516.0"), secondBar.getOpen());
        assertEquals(new Price("16579.2"), secondBar.getHigh());
        assertEquals(new Price("16155.8"), secondBar.getLow());
        assertEquals(new Price("16276.6"), secondBar.getClose());
        assertEquals(255990, secondBar.getVolume());
    }

    @Test
    void testStopDataFeed() throws InterruptedException {
        CsvDataFeed dataFeed = new CsvDataFeed(csvFile.getAbsolutePath(), format, DataFeedSpeed.INSTANT);
        List<Bar> receivedBars = new ArrayList<>();

        dataFeed.addBarDataListener(receivedBars::add);

        Thread feedThread = new Thread(() -> {
            try {
                dataFeed.start();
            } catch (DataFeedException e) {
                e.printStackTrace();
            }
        });
        feedThread.start();

        Thread.sleep(100);

        dataFeed.stop();
        feedThread.join(1000);

        assertFalse(receivedBars.isEmpty(), "Should have received at least 1 bar before stopping");
        assertFalse(feedThread.isAlive(), "Feed thread should have stopped");
    }
}