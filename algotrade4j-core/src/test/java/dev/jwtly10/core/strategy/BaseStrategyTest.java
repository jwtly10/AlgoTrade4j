package dev.jwtly10.core.strategy;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.indicators.Indicator;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class BaseStrategyTest {

    @Mock
    private BarSeries mockBarSeries;
    @Mock
    private DataManager mockDataManager;
    @Mock
    private TradeManager mockTradeManager;
    @Mock
    private AccountManager mockAccountManager;
    @Mock
    private EventPublisher mockEventPublisher;
    @Mock
    private PerformanceAnalyser mockPerformanceAnalyser;

    private TestStrategy testStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testStrategy = new TestStrategy("TEST001");
    }

    @Test
    void testIndicatorsNotInitialized() {
        // Assert that the indicator setup in the strategy is null before running onInit
        assertNull(testStrategy.getTestIndicator());
    }

    @Test
    void testOnInit() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser);

        assertEquals(mockBarSeries, testStrategy.getBarSeries());
        assertEquals(mockDataManager, testStrategy.getDataManager());
        assertEquals(mockTradeManager, testStrategy.getTradeManager());
        assertEquals(mockEventPublisher, testStrategy.getEventPublisher());
        assertEquals(mockPerformanceAnalyser, testStrategy.getPerformanceAnalyser());
        // Assert that the indicator was created
        assertNotNull(testStrategy.getTestIndicator());
    }


    @Test
    void testGetStrategyId() {
        assertEquals("TEST001", testStrategy.getStrategyId());
    }

    @Test
    void testCreateIndicator() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser);

        var indicator = testStrategy.getTestIndicator();

        assertNotNull(indicator);
        assertEquals(5, indicator.getIntParam());
        assertEquals(10.5, indicator.getDoubleParam(), 0.001);
        assertEquals(mockEventPublisher, indicator.getEventPublisher());
        assertEquals("TEST001", indicator.getStrategyId());
    }

    @Test
    void testCreateIndicatorIncorrectParams() {
        assertThrows(RuntimeException.class, () -> testStrategy.createIndicator(TestIndicator.class, "Invalid int", 10.5));
    }

    // Test strategy implementation
    @Getter
    private static class TestStrategy extends BaseStrategy {
        private TestIndicator testIndicator;

        public TestStrategy(String strategyId) {
            super(strategyId);
        }

        @Override
        protected void initIndicators() {
            testIndicator = createIndicator(TestIndicator.class, 5, 10.5);
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

    // Test indicator implementation
    @Getter
    private static class TestIndicator implements Indicator {
        private final int intParam;
        private final double doubleParam;
        private EventPublisher eventPublisher;
        private String strategyId;

        public TestIndicator(int intParam, double doubleParam) {
            this.intParam = intParam;
            this.doubleParam = doubleParam;
        }

        @Override
        public void update(Bar bar) {

        }

        @Override
        public Number getValue() {
            return null;
        }

        @Override
        public Number getValue(int index) {
            return null;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public int getRequiredPeriods() {
            return 0;
        }

        @Override
        public void setEventPublisher(EventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        @Override
        public void setStrategyId(String strategyId) {
            this.strategyId = strategyId;
        }
    }
}