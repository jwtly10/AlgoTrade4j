package dev.jwtly10.core.dataimport;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCsvFormatTest {

    private DefaultCsvFormat format;

    @BeforeEach
    void setUp() {
        format = new DefaultCsvFormat(Duration.ofMinutes(1));
    }

    @Test
    void testParseBar() {
        String[] fields = {"2022.01.02T22:00", "16419.7", "16526.0", "16310.6", "16512.8", "209249"};

        Bar bar = format.parseBar(fields);

        assertNotNull(bar);
        assertEquals(LocalDateTime.of(2022, 1, 2, 22, 0), bar.getDateTime());
        assertEquals(new Price(16419.7), bar.getOpen());
        assertEquals(new Price(16526.0), bar.getHigh());
        assertEquals(new Price(16310.6), bar.getLow());
        assertEquals(new Price(16512.8), bar.getClose());
        assertEquals(209249, bar.getVolume());
        assertEquals(Duration.ofMinutes(1), bar.getTimePeriod());
    }

    @Test
    void testHasHeader() {
        assertTrue(format.hasHeader());
    }

    @Test
    void testGetDelimiter() {
        assertEquals(",", format.getDelimiter());
    }

    @Test
    void testGetTimePeriod() {
        assertEquals(Duration.ofMinutes(1), format.getTimePeriod());
    }
}