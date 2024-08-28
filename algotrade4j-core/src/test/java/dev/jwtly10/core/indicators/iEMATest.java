package dev.jwtly10.core.indicators;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class iEMATest {

    private static final int PERIOD = 10;
    private iEMA ema;

    @BeforeEach
    void setUp() {
        ema = new iEMA(PERIOD);
    }

    @Test
    void testInitialState() {
        assertFalse(ema.isReady());
        assertEquals(Number.ZERO, ema.getValue());
        assertEquals(PERIOD, ema.getRequiredPeriods());
        assertEquals("EMA " + PERIOD, ema.getName());
    }

    @Test
    void testFirstUpdate() {
        Bar bar = createBar(100);
        ema.update(bar);

        assertTrue(ema.isReady());
        assertEquals(new Number(BigDecimal.valueOf(100)), ema.getValue());
    }

    @Test
    void testEMACalculation() {
        ema.update(createBar(100));

        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (PERIOD + 1));
        BigDecimal expectedEMA = BigDecimal.valueOf(100);

        for (int i = 1; i <= 20; i++) {
            double price = 100 + i * 10;
            ema.update(createBar(price));

            expectedEMA = BigDecimal.valueOf(price).multiply(multiplier)
                    .add(expectedEMA.multiply(BigDecimal.ONE.subtract(multiplier)));

            BigDecimal actualValue = ema.getValue().getValue();
            BigDecimal expectedValue = expectedEMA.setScale(Number.DECIMAL_PLACES, Number.ROUNDING_MODE);

            assertTrue(
                    actualValue.subtract(expectedValue).abs().compareTo(BigDecimal.valueOf(0.002)) < 0,
                    String.format("EMA calculation incorrect at step %d. Expected: %s, Actual: %s",
                            i, expectedValue, actualValue)
            );
        }
    }

    @Test
    void testHistoricalValues() {
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (PERIOD + 1));
        BigDecimal ema = BigDecimal.ZERO;
        List<BigDecimal> expectedValues = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            BigDecimal price = BigDecimal.valueOf(100 + i * 10);
            this.ema.update(createBar(price.doubleValue()));

            if (i == 0) {
                ema = price;
            } else {
                ema = price.multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
            }
            expectedValues.add(ema);
        }

        for (int i = 4; i >= 0; i--) {
            BigDecimal expected = expectedValues.get(i).setScale(Number.DECIMAL_PLACES, Number.ROUNDING_MODE);
            BigDecimal actual = this.ema.getValue(4 - i).getValue();

            assertTrue(
                    actual.subtract(expected).abs().compareTo(BigDecimal.valueOf(0.002)) < 0,
                    String.format("EMA historical value incorrect at index %d. Expected: %s, Actual: %s",
                            4 - i, expected, actual)
            );
        }
    }

    @Test
    void testInvalidHistoricalIndex() {
        ema.update(createBar(100));
        assertThrows(IndexOutOfBoundsException.class, () -> ema.getValue(1));
        assertThrows(IndexOutOfBoundsException.class, () -> ema.getValue(-1));
    }

    private Bar createBar(double closePrice) {
        return DefaultBar.builder()
                .close(new Number(BigDecimal.valueOf(closePrice)))
                .openTime(ZonedDateTime.now())
                .instrument(Instrument.NAS100USD)
                .build();
    }
}