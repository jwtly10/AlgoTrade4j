package dev.jwtly10.core.indicators;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.BaseStrategy;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndicatorUtilsTest {

    private TestStrategy testStrategy;

    @BeforeEach
    void setUp() {
        testStrategy = new TestStrategy("test-strategy");
        testStrategy.initIndicators();
    }

    @Test
    void testUpdateIndicators() {
        Bar testBar = new DefaultBar(
                Instrument.NAS100USD,
                Duration.ofMinutes(1),
                ZonedDateTime.now(),
                new Number(150.0),
                new Number(151.0),
                new Number(149.0),
                new Number(150.5),
                new Number(1000)
        );

        assertNotNull(testStrategy.getTestIndicator());

        IndicatorUtils.updateIndicators(testStrategy, testBar);

        assertTrue(testStrategy.getTestIndicator().wasUpdated());
    }

    @Test
    void testGetIndicators() {
        List<Indicator> indicators = IndicatorUtils.getIndicators(testStrategy);

        assertEquals(1, indicators.size());

        assertTrue(indicators.get(0) instanceof DNUTestIndicator);

        assertSame(testStrategy.getTestIndicator(), indicators.get(0));
    }

    @Getter
    private static class TestStrategy extends BaseStrategy {
        private DNUTestIndicator testIndicator;

        public TestStrategy(String strategyId) {
            super(strategyId);
        }

        @Override
        protected void initIndicators() {
            testIndicator = createIndicator(DNUTestIndicator.class, 5, 10.5);
        }

        @Override
        public void onBarClose(Bar bar) {
            // Not needed for this test
        }

        @Override
        public void onTick(Tick tick, Bar currentBar) {
            // Not needed for this test
        }

        @Override
        public void onDeInit() {
            // Not needed for this test
        }
    }

    public static class DNUTestIndicator implements Indicator {
        private final int period;
        private final double initialValue;
        private boolean updated = false;
        private Number currentValue;

        public DNUTestIndicator(int period, double initialValue) {
            this.period = period;
            this.initialValue = initialValue;
            this.currentValue = new Number(initialValue);
        }

        @Override
        public void update(Bar bar) {
            updated = true;
            currentValue = bar.getClose();
        }

        @Override
        public Number getValue() {
            return currentValue;
        }

        @Override
        public Number getValue(int index) {
            return currentValue; // Simplified for this test
        }

        @Override
        public String getName() {
            return "TestIndicator";
        }

        @Override
        public boolean isReady() {
            return true; // Simplified for this test
        }

        @Override
        public int getRequiredPeriods() {
            return period;
        }

        @Override
        public void setEventPublisher(EventPublisher eventPublisher) {

        }

        @Override
        public void setStrategyId(String strategyId) {

        }

        public boolean wasUpdated() {
            return updated;
        }
    }
}