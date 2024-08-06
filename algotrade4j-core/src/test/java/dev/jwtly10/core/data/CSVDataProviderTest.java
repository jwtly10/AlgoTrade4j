package dev.jwtly10.core.data;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CSVDataProviderTest {
    private final Number spread = new Number("0.02");
    @TempDir
    Path tempDir;
    private List<Tick> capturedTicks;

    @BeforeEach
    void setUp() {
        capturedTicks = new ArrayList<>();
    }

    @Test
    void testSeedReproducibility() throws IOException {
        String csvContent =
                "Date,Open,High,Low,Close,Volume\n" +
                        "2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000\n" +
                        "2023.01.01T00:01:00+00:00,102.0,106.0,101.0,104.0,1200\n" +
                        "2023.01.01T00:02:00+00:00,104.0,107.0,103.0,105.0,1100\n";

        long seed = 12345L;
        List<Tick> ticks1 = generateTicks(csvContent, seed);
        List<Tick> ticks2 = generateTicks(csvContent, seed);

        assertEquals(ticks1.size(), ticks2.size());
        for (int i = 0; i < ticks1.size(); i++) {
            assertEquals(ticks1.get(i).getMid(), ticks2.get(i).getMid());
            assertEquals(ticks1.get(i).getVolume(), ticks2.get(i).getVolume());
        }
    }

    @Test
    void testRandomness() throws IOException {
        String csvContent =
                """
                        Date,Open,High,Low,Close,Volume
                        2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000
                        2023.01.01T00:01:00+00:00,102.0,106.0,101.0,104.0,1200
                        2023.01.01T00:02:00+00:00,104.0,107.0,103.0,105.0,1100
                        """;

        List<Tick> ticks1 = generateTicks(csvContent, 12345L);
        List<Tick> ticks2 = generateTicks(csvContent, 67890L);

        assertEquals(ticks1.size(), ticks2.size());
        boolean allEqual = true;
        for (int i = 0; i < ticks1.size(); i++) {
            if (!ticks1.get(i).getMid().equals(ticks2.get(i).getMid()) ||
                    !ticks1.get(i).getVolume().equals(ticks2.get(i).getVolume())) {
                allEqual = false;
                break;
            }
        }
        assertFalse(allEqual, "Ticks should be different with different seeds");
    }

    @Test
    void testPricesWithinRange() throws IOException {
        String csvContent =
                """
                        Date,Open,High,Low,Close,Volume
                        2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000
                        """;

        List<Tick> ticks = generateTicks(csvContent, 12345L);

        assertEquals(100.0, ticks.getFirst().getMid().getValue().doubleValue(), 0.0001);
        assertEquals(102.0, ticks.getLast().getMid().getValue().doubleValue(), 0.0001);

        for (int i = 1; i < ticks.size() - 1; i++) {
            double price = ticks.get(i).getMid().getValue().doubleValue();
            assertTrue(price >= 98.0 && price <= 105.0, "Price should be within range");
        }
    }

    private List<Tick> generateTicks(String csvContent, long seed) throws IOException {
        Path csvFile = tempDir.resolve("test_data.csv");
        Files.writeString(csvFile, csvContent);

        List<Tick> ticks = new ArrayList<>();
        CSVDataProvider provider = new CSVDataProvider(csvFile.toString(), 5, spread, Duration.ofMinutes(1), seed);
        provider.addDataProviderListener(new DataProviderListener() {
            @Override
            public void onTick(Tick tick) {
                ticks.add(tick);
            }

            @Override
            public void onStop() {
                // Not needed for this test
            }
        });
        provider.start();
        return ticks;
    }

    @Test
    void testTickGenerationOneMinute() throws IOException {
        String csvContent =
                """
                        Date,Open,High,Low,Close,Volume
                        2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000
                        2023.01.01T00:01:00+00:00,102.0,106.0,101.0,104.0,1200
                        2023.01.01T00:02:00+00:00,104.0,107.0,103.0,105.0,1100
                        """;
        testTickGeneration(Duration.ofMinutes(1), 5, 3, csvContent);
    }

    @Test
    void testTickGenerationFiveMinutes() throws IOException {
        String csvContent =
                """
                        Date,Open,High,Low,Close,Volume
                        2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000
                        2023.01.01T00:05:00+00:00,102.0,106.0,101.0,104.0,1200
                        2023.01.01T00:10:00+00:00,104.0,107.0,103.0,105.0,1100
                        """;
        testTickGeneration(Duration.ofMinutes(5), 5, 3, csvContent);
    }

    @Test
    void testTickGenerationFifteenMinutes() throws IOException {
        String csvContent =
                """
                        Date,Open,High,Low,Close,Volume
                        2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000
                        2023.01.01T00:15:00+00:00,102.0,106.0,101.0,104.0,1200
                        2023.01.01T00:30:00+00:00,104.0,107.0,103.0,105.0,1100
                        """;
        testTickGeneration(Duration.ofMinutes(15), 5, 3, csvContent);
    }

    @Test
    void testTickGenerationOneHour() throws IOException {
        String csvContent =
                """
                        Date,Open,High,Low,Close,Volume
                        2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000
                        2023.01.01T01:00:00+00:00,102.0,106.0,101.0,104.0,1200
                        2023.01.01T02:00:00+00:00,104.0,107.0,103.0,105.0,1100
                        """;
        testTickGeneration(Duration.ofHours(1), 5, 3, csvContent);
    }

    void testTickGeneration(Duration period, int ticksPerBar, int expectedBars, String csvContent) throws IOException {
        Path csvFile = tempDir.resolve("test_data.csv");
        Files.writeString(csvFile, csvContent);

        capturedTicks.clear();
        CSVDataProvider provider = new CSVDataProvider(csvFile.toString(), ticksPerBar, spread, period);
        provider.addDataProviderListener(new DataProviderListener() {
            @Override
            public void onTick(Tick tick) {
                capturedTicks.add(tick);
            }

            @Override
            public void onStop() {
                // Not needed for this test
            }
        });
        provider.start();

        assertEquals(expectedBars * ticksPerBar, capturedTicks.size());

        for (int i = 0; i < expectedBars; i++) {
            testBar(i, provider.getDateTimeFormatter(), ticksPerBar, period);
        }
    }

    private void testBar(int barIndex, DateTimeFormatter dateTimeFormatter, int ticksPerBar, Duration period) {
        ZonedDateTime openTime = ZonedDateTime.parse("2023.01.01T00:00:00+00:00", dateTimeFormatter).plus(period.multipliedBy(barIndex));

        for (int i = 0; i < ticksPerBar; i++) {
            Tick tick = capturedTicks.get(barIndex * ticksPerBar + i);

            // Test timestamp
            Duration expectedDuration = Duration.ofNanos((period.toNanos() * i) / (ticksPerBar - 1));
            assertEquals(openTime.plus(expectedDuration), tick.getDateTime());
        }
    }

    @Test
    void testEdgeCases() throws IOException {
        String csvContent =
                "Date,Open,High,Low,Close,Volume\n" +
                        "2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000\n" +
                        "2023.01.01T00:01:00+00:00,102.0,106.0,101.0,104.0,1200\n" +
                        "2023.01.01T00:02:00+00:00,104.0,107.0,103.0,105.0,1100\n";

        // Test with 1 tick per bar
        testEdgeCase(1, Duration.ofMinutes(1), 3, csvContent);

        // Test with very short duration
        testEdgeCase(3, Duration.ofMinutes(1), 3, csvContent);
    }

    private void testEdgeCase(int ticksPerBar, Duration period, int expectedBars, String csvContent) throws IOException {
        Path csvFile = tempDir.resolve("test_data.csv");
        Files.writeString(csvFile, csvContent);

        capturedTicks.clear();
        CSVDataProvider provider = new CSVDataProvider(csvFile.toString(), ticksPerBar, new Number("0.01"), period);
        provider.addDataProviderListener(new DataProviderListener() {
            @Override
            public void onTick(Tick tick) {
                capturedTicks.add(tick);
            }

            @Override
            public void onStop() {
                // Not needed for this test
            }
        });
        provider.start();

        assertEquals(expectedBars * ticksPerBar, capturedTicks.size());

        for (int i = 0; i < expectedBars; i++) {
            ZonedDateTime expectedBaseTime = ZonedDateTime.parse("2023.01.01T00:00:00+00:00", provider.getDateTimeFormatter()).plus(period.multipliedBy(i));
            for (int j = 0; j < ticksPerBar; j++) {
                Tick tick = capturedTicks.get(i * ticksPerBar + j);
                Duration expectedOffset = Duration.ofNanos((period.toNanos() * j) / Math.max(1, ticksPerBar - 1));
                assertEquals(expectedBaseTime.plus(expectedOffset), tick.getDateTime());
            }
        }
    }
}