package dev.jwtly10.core.data;

import dev.jwtly10.core.exception.DataProviderException;
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

import static dev.jwtly10.core.model.Instrument.NAS100USD;
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
    void testSeedReproducibility() throws IOException, DataProviderException {
        String csvContent =
                "Date,Open,High,Low,Close,Volume\n" +
                        "2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000\n" +
                        "2023.01.01T00:01:00+00:00,102.0,106.0,101.0,104.0,1200\n" +
                        "2023.01.01T00:02:00+00:00,104.0,107.0,103.0,105.0,1100\n";

        long seed = 12345L;
        List<Tick> ticks1 = generateTicks(csvContent, 5, seed);
        List<Tick> ticks2 = generateTicks(csvContent, 5, seed);

        assertEquals(ticks1.size(), ticks2.size());
        for (int i = 0; i < ticks1.size(); i++) {
            assertEquals(ticks1.get(i).getMid(), ticks2.get(i).getMid());
            assertEquals(ticks1.get(i).getVolume(), ticks2.get(i).getVolume());
        }
    }

    @Test
    void testRandomness() throws IOException, DataProviderException {
        String csvContent =
                "Date,Open,High,Low,Close,Volume\n" +
                        "2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000\n" +
                        "2023.01.01T00:01:00+00:00,102.0,106.0,101.0,104.0,1200\n" +
                        "2023.01.01T00:02:00+00:00,104.0,107.0,103.0,105.0,1100\n";

        List<Tick> ticks1 = generateTicks(csvContent, 5, 12345L);
        List<Tick> ticks2 = generateTicks(csvContent, 5, 67890L);

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
    void testPricesWithinRange() throws IOException, DataProviderException {
        String csvContent =
                "Date,Open,High,Low,Close,Volume\n" +
                        "2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000\n";

        List<Tick> ticks = generateTicks(csvContent, 4, 12345L);

        assertEquals(100.0, ticks.getFirst().getMid().getValue().doubleValue(), 0.0001);
        assertEquals(102.0, ticks.getLast().getMid().getValue().doubleValue(), 0.0001);

        boolean hitHigh = false;
        boolean hitLow = false;

        for (int i = 1; i < ticks.size() - 1; i++) {
            double price = ticks.get(i).getMid().getValue().doubleValue();
            assertTrue(price >= 98.0 && price <= 105.0, "Price should be within range");

            if (Math.abs(price - 105.0) < 0.0001) hitHigh = true;
            if (Math.abs(price - 98.0) < 0.0001) hitLow = true;
        }

        assertTrue(hitHigh, "Should hit high price");
        assertTrue(hitLow, "Should hit low price");
    }

    @Test
    void testPricesWithinRangeAdditionalTicks() throws IOException, DataProviderException {
        String csvContent =
                "Date,Open,High,Low,Close,Volume\n" +
                        "2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000\n";

        // We should use different ticks every time to be sure
        List<Tick> ticks = generateTicks(csvContent, 10, System.currentTimeMillis());

        assertEquals(100.0, ticks.getFirst().getMid().getValue().doubleValue(), 0.0001);
        assertEquals(102.0, ticks.getLast().getMid().getValue().doubleValue(), 0.0001);

        boolean hitHigh = false;
        boolean hitLow = false;

        for (int i = 1; i < ticks.size() - 1; i++) {
            double price = ticks.get(i).getMid().getValue().doubleValue();
            assertTrue(price >= 98.0 && price <= 105.0, "Price should be within range");

            if (Math.abs(price - 105.0) < 0.0001) hitHigh = true;
            if (Math.abs(price - 98.0) < 0.0001) hitLow = true;
        }

        assertTrue(hitHigh, "Should hit high price");
        assertTrue(hitLow, "Should hit low price");
    }

    @Test
    void testTickGenerationWithVariousPeriods() throws IOException, DataProviderException {
        testTickGeneration(Duration.ofMinutes(1), 5, 3);
        testTickGeneration(Duration.ofMinutes(5), 5, 3);
        testTickGeneration(Duration.ofMinutes(15), 5, 3);
        testTickGeneration(Duration.ofHours(1), 5, 3);
        testTickGeneration(Duration.ofDays(1), 5, 3);
    }

    @Test
    void testTickGenerationWithMissingData() throws IOException, DataProviderException {
        String csvContent =
                "Date,Open,High,Low,Close,Volume\n" +
                        "2023.01.01T00:00:00+00:00,100.0,105.0,98.0,102.0,1000\n" +
                        "2023.01.04T00:00:00+00:00,104.0,107.0,103.0,105.0,1100\n";

        List<Tick> ticks = generateTicks(csvContent, 5, 12345L);

        assertEquals(10, ticks.size(), "Should generate ticks for 2 days only");

        ZonedDateTime firstTickTime = ticks.get(0).getDateTime();
        ZonedDateTime lastTickTime = ticks.get(9).getDateTime();

        assertEquals(3, Duration.between(firstTickTime, lastTickTime).toDays(),
                "Should skip 2 days between first and last tick");
    }

    @Test
    void testEdgeCases() throws IOException, DataProviderException {
        // Here we are checking OHLC prices and timestamps for any edge cases in the random tick generation logic
        // Ensuring they are all being hit

        // Since tick generation is random, we run this multiple times to ensure ample opportunity to meet edge cases.
        // This will model 100k+ ticks, validating all OHCL vals are met
        int i;
        for (i = 0; i < 100; i++) {
            testEdgeCase(5, Duration.ofMinutes(1), 3);
            // Test with minimum allowed ticks per bar (4)
            testEdgeCase(4, Duration.ofMinutes(1), 3);

            // Test with a large number of ticks per bar
            testEdgeCase(100, Duration.ofMinutes(1), 3);

            // Test with an even larger number of ticks per bar
            testEdgeCase(1000, Duration.ofMinutes(1), 3);

            // Test with a very short duration
            testEdgeCase(5, Duration.ofSeconds(1), 3);

            // Test with a very long duration
            testEdgeCase(5, Duration.ofDays(7), 3);
        }
    }

    @Test
    void testDataProviderThrows() {
        CSVDataProvider provider = new CSVDataProvider("INVALID_FILE_PATH", 5, new Number("0.01"), Duration.ofMinutes(1), NAS100USD);
        assertThrows(DataProviderException.class, provider::start);
    }

    private void testEdgeCase(int ticksPerBar, Duration period, int expectedBars) throws IOException, DataProviderException {
        String csvContent = generateCsvContent(period, expectedBars);
        Path csvFile = tempDir.resolve("test_data.csv");
        Files.writeString(csvFile, csvContent);

        capturedTicks.clear();
        CSVDataProvider provider = new CSVDataProvider(csvFile.toString(), ticksPerBar, new Number("0.01"), period, NAS100USD);
        provider.addDataProviderListener(new DataProviderListener() {
            @Override
            public void onTick(Tick tick) {
                capturedTicks.add(tick);
            }

            @Override
            public void onStop() {
                // Not needed for this test
            }

            @Override
            public void onError(DataProviderException e) {
                // Not needed for this test
            }
        });
        provider.setDataSpeed(DataSpeed.INSTANT);
        provider.start();

        assertEquals(expectedBars * ticksPerBar, capturedTicks.size(),
                "Expected " + (expectedBars * ticksPerBar) + " ticks, but got " + capturedTicks.size());

        ZonedDateTime expectedBaseTime = ZonedDateTime.parse("2023.01.01T00:00:00+00:00", provider.getDateTimeFormatter());
        for (int i = 0; i < expectedBars; i++) {
            for (int j = 0; j < ticksPerBar; j++) {
                Tick tick = capturedTicks.get(i * ticksPerBar + j);
                Duration expectedOffset = Duration.ofNanos((period.toNanos() * j) / (ticksPerBar - 1));
                ZonedDateTime expectedTime = expectedBaseTime.plus(period.multipliedBy(i)).plus(expectedOffset);
                // If its the last bar, should minus a second from the "period close"
                if (j == ticksPerBar - 1) {
                    expectedTime = expectedTime.minusSeconds(1);
                }
                assertEquals(expectedTime, tick.getDateTime(),
                        "Tick " + (i * ticksPerBar + j) + " has incorrect timestamp");
            }
        }

        // Check if open, high, low, and close prices are hit
        for (int i = 0; i < expectedBars; i++) {
            int startIndex = i * ticksPerBar;
            int endIndex = (i + 1) * ticksPerBar;
            List<Tick> barTicks = capturedTicks.subList(startIndex, endIndex);

            assertEquals(100.0, barTicks.getFirst().getMid().getValue().doubleValue(), 0.0001, "First tick should be open price");
            assertEquals(102.0, barTicks.get(ticksPerBar - 1).getMid().getValue().doubleValue(), 0.0001, "Last tick should be close price");

            boolean hitHigh = false;
            boolean hitLow = false;
            for (Tick tick : barTicks) {
                double price = tick.getMid().getValue().doubleValue();
                if (price == 105.0) hitHigh = true;
                if (price == 98.0) hitLow = true;
            }
            assertTrue(hitHigh, "Should hit high price in bar " + i);
            assertTrue(hitLow, "Should hit low price in bar " + i);
        }
    }


    private List<Tick> generateTicks(String csvContent, int ticksPerBar, long seed) throws IOException, DataProviderException {
        Path csvFile = tempDir.resolve("test_data.csv");
        Files.writeString(csvFile, csvContent);

        List<Tick> ticks = new ArrayList<>();
        CSVDataProvider provider = new CSVDataProvider(csvFile.toString(), ticksPerBar, spread, Duration.ofMinutes(1), NAS100USD, seed);
        provider.addDataProviderListener(new DataProviderListener() {
            @Override
            public void onTick(Tick tick) {
                ticks.add(tick);
            }

            @Override
            public void onStop() {
                // Not needed for this test
            }

            @Override
            public void onError(DataProviderException e) {
                // Not needed for this test
            }
        });
        provider.setDataSpeed(DataSpeed.INSTANT);
        provider.start();
        return ticks;
    }

    void testTickGeneration(Duration period, int ticksPerBar, int expectedBars) throws IOException, DataProviderException {
        String csvContent = generateCsvContent(period, expectedBars);
        Path csvFile = tempDir.resolve("test_data.csv");
        Files.writeString(csvFile, csvContent);

        capturedTicks.clear();
        CSVDataProvider provider = new CSVDataProvider(csvFile.toString(), ticksPerBar, spread, period, NAS100USD);
        provider.addDataProviderListener(new DataProviderListener() {
            @Override
            public void onTick(Tick tick) {
                capturedTicks.add(tick);
            }

            @Override
            public void onStop() {
                // Not needed for this test
            }

            @Override
            public void onError(DataProviderException e) {
                // Not needed for this test
            }
        });
        provider.setDataSpeed(DataSpeed.INSTANT);
        provider.start();

        assertEquals(expectedBars * ticksPerBar, capturedTicks.size());

        for (int i = 0; i < expectedBars; i++) {
            testBar(i, provider.getDateTimeFormatter(), ticksPerBar, period);
        }
    }

    private String generateCsvContent(Duration period, int bars) {
        StringBuilder sb = new StringBuilder("Date,Open,High,Low,Close,Volume\n");
        ZonedDateTime baseTime = ZonedDateTime.parse("2023.01.01T00:00:00+00:00", DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ssXXX"));
        for (int i = 0; i < bars; i++) {
            ZonedDateTime time = baseTime.plus(period.multipliedBy(i));
            sb.append(String.format("%s,100.0,105.0,98.0,102.0,1000\n", time.format(DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ssXXX"))));
        }
        return sb.toString();
    }

    private void testBar(int barIndex, DateTimeFormatter dateTimeFormatter, int ticksPerBar, Duration period) {
        ZonedDateTime openTime = ZonedDateTime.parse("2023.01.01T00:00:00+00:00", dateTimeFormatter).plus(period.multipliedBy(barIndex));

        boolean hitHigh = false;
        boolean hitLow = false;

        for (int i = 0; i < ticksPerBar; i++) {
            Tick tick = capturedTicks.get(barIndex * ticksPerBar + i);

            // Test timestamp
            Duration expectedDuration = Duration.ofNanos((period.toNanos() * i) / (ticksPerBar - 1));

            // If its the last bar, it should be a second before the 'close'
            if (i == ticksPerBar - 1) {
                expectedDuration = expectedDuration.minusSeconds(1);
            }

            assertEquals(openTime.plus(expectedDuration), tick.getDateTime());

            // Check if high and low are hit
            if (Math.abs(tick.getMid().getValue().doubleValue() - 105.0) < 0.0001) hitHigh = true;
            if (Math.abs(tick.getMid().getValue().doubleValue() - 98.0) < 0.0001) hitLow = true;
        }

        assertTrue(hitHigh, "Should hit high price in bar " + barIndex);
        assertTrue(hitLow, "Should hit low price in bar " + barIndex);
    }

}