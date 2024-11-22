package dev.jwtly10.core.strategy;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.external.news.StrategyNewsUtil;
import dev.jwtly10.core.indicators.Indicator;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.risk.RiskProfile;
import dev.jwtly10.core.risk.RiskProfileConfig;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
    private StrategyNewsUtil strategyNewsUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.strategyNewsUtil = new StrategyNewsUtil();
        testStrategy = new TestStrategy("TEST001");
    }

    @Test
    void testIndicatorsNotInitialized() {
        // Assert that the indicator setup in the strategy is null before running onInit
        assertNull(testStrategy.getTestIndicator());
    }

    @Test
    void testOnInit() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser, null, strategyNewsUtil);

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
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser, null, strategyNewsUtil);
        when(mockTradeManager.getBroker()).thenReturn(Broker.OANDA);

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

    @Test
    void testGBPUSDLongStopLoss() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser, null, strategyNewsUtil);
        when(mockTradeManager.getBroker()).thenReturn(Broker.OANDA);

        Instrument instrument = Instrument.GBPUSD;
        Number price = new Number("1.30000");
        int ticks = 50;
        boolean isLong = true;

        Number expectedStopLoss = new Number("1.29950");
        Number actualStopLoss = testStrategy.getStopLossGivenInstrumentPriceDir(instrument, price, ticks, isLong);

        assertEquals(expectedStopLoss, actualStopLoss);
    }

    /**
     * This is a 'real' trade taken on 19th Sep 2024.
     * If this fails. Something is wrong. This data was validated on trading view charts for OANDA.
     */
    @Test
    void testGBPUSDRealLongStopLoss() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser, null, strategyNewsUtil);
        when(mockTradeManager.getBroker()).thenReturn(Broker.OANDA);

        Instrument instrument = Instrument.GBPUSD;
        Number price = new Number("1.32631");
        int ticks = 300;
        boolean isLong = true;

        Number expectedStopLoss = new Number("1.32331");
        Number actualStopLoss = testStrategy.getStopLossGivenInstrumentPriceDir(instrument, price, ticks, isLong);

        assertEquals(expectedStopLoss, actualStopLoss);
    }

    @Test
    void testGBPUSDShortStopLoss() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser, null, strategyNewsUtil);
        when(mockTradeManager.getBroker()).thenReturn(Broker.OANDA);

        Instrument instrument = Instrument.GBPUSD;
        Number price = new Number("1.30000");
        int ticks = 50;
        boolean isLong = false;

        Number expectedStopLoss = new Number("1.30050");
        Number actualStopLoss = testStrategy.getStopLossGivenInstrumentPriceDir(instrument, price, ticks, isLong);

        assertEquals(expectedStopLoss, actualStopLoss);
    }

    /**
     * This is a 'real' trade taken on 19th Sep 2024.
     * If this fails. Something is wrong. This data was validated on trading view charts for OANDA.
     */
    @Test
    void testNAS100USDRealLongStopLoss() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser, null, strategyNewsUtil);
        when(mockTradeManager.getBroker()).thenReturn(Broker.OANDA);

        Instrument instrument = Instrument.NAS100USD;
        Number price = new Number("19814.2");
        int ticks = 300;
        boolean isLong = true;

        Number expectedStopLoss = new Number("19784.2");
        Number actualStopLoss = testStrategy.getStopLossGivenInstrumentPriceDir(instrument, price, ticks, isLong);

        assertEquals(expectedStopLoss, actualStopLoss);
    }


    @Test
    void testNAS100USDLongStopLoss() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser, null, strategyNewsUtil);
        when(mockTradeManager.getBroker()).thenReturn(Broker.OANDA);

        Instrument instrument = Instrument.NAS100USD;
        Number price = new Number("15000.0");
        int ticks = 50;
        boolean isLong = true;

        Number expectedStopLoss = new Number("14995.0");
        Number actualStopLoss = testStrategy.getStopLossGivenInstrumentPriceDir(instrument, price, ticks, isLong);

        assertEquals(expectedStopLoss, actualStopLoss);
    }

    @Test
    void testNAS100USDShortStopLoss() {
        testStrategy.onInit(mockBarSeries, mockDataManager, mockAccountManager, mockTradeManager, mockEventPublisher, mockPerformanceAnalyser, null, strategyNewsUtil);
        when(mockTradeManager.getBroker()).thenReturn(Broker.OANDA);

        Instrument instrument = Instrument.NAS100USD;
        Number price = new Number("15000.0");
        int ticks = 50;
        boolean isLong = false;

        Number expectedStopLoss = new Number("15005.0");
        Number actualStopLoss = testStrategy.getStopLossGivenInstrumentPriceDir(instrument, price, ticks, isLong);

        assertEquals(expectedStopLoss, actualStopLoss);
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
        public RiskProfileConfig getRiskProfileConfig() {
            return RiskProfile.NONE.getConfig();
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
        public List<IndicatorValue> getValues() {
            return List.of();
        }

        @Override
        public void update(Bar bar) {

        }

        @Override
        public double getValue() {
            return 0.0;
        }

        @Override
        public double getValue(int index) {
            return 0.0;
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