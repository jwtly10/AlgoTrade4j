package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.AccountEvent;
import dev.jwtly10.core.event.types.AnalysisEvent;
import dev.jwtly10.core.event.types.LogEvent;
import dev.jwtly10.core.event.types.StrategyStopEvent;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.risk.RiskManager;
import dev.jwtly10.core.strategy.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BacktestExecutorTest {

    @Mock
    private Strategy strategy;
    @Mock
    private TradeManager tradeManager;
    @Mock
    private TradeStateManager tradeStateManager;
    @Mock
    private AccountManager accountManager;
    @Mock
    private DataManager dataManager;
    @Mock
    private BarSeries barSeries;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private PerformanceAnalyser performanceAnalyser;
    @Mock
    private RiskManager riskManager;

    private BacktestExecutor backtestExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(strategy.getStrategyId()).thenReturn("testStrategy");
        backtestExecutor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser, riskManager);
    }

    @Test
    void testInitialisation() {
        assertFalse(backtestExecutor.isInitialised());
        backtestExecutor.initialise();
        assertTrue(backtestExecutor.isInitialised());
        verify(strategy).onStart();
        verify(eventPublisher).publishEvent(any(LogEvent.class));
    }

    @Test
    void testDoubleInitialisation() {
        backtestExecutor.initialise();
        backtestExecutor.initialise(); // Should not re-initialize
        verify(strategy, times(1)).onStart();
    }

    @Test
    void testOnTick() {
        Tick tick = mock(Tick.class);
        Bar currentBar = mock(Bar.class);
        backtestExecutor.initialise();
        when(accountManager.getEquity()).thenReturn(10000.0);
        backtestExecutor.onTick(tick, currentBar);
        verify(strategy).onTick(tick, currentBar);
        verify(tradeManager).setCurrentTick(tick);
        verify(tradeStateManager).updateTradeProfitStateOnTick(tradeManager, tick);
        when(accountManager.getEquity()).thenReturn(1000.0);
    }

    @Test
    void testOnBarClose() {
        Bar closedBar = mock(Bar.class);
        backtestExecutor.initialise();
        when(accountManager.getEquity()).thenReturn(10000.0);
        backtestExecutor.onBarClose(closedBar);
        verify(strategy).onBarClose(closedBar);
    }

    @Test
    void testOnStop() {
        backtestExecutor.initialise();
        when(tradeManager.getOpenTrades()).thenReturn(new ConcurrentHashMap<>());
        when(accountManager.getInitialBalance()).thenReturn(10000.0);
        backtestExecutor.onStop();
        verify(strategy).onDeInit();
        verify(strategy).onEnd();
        verify(eventPublisher).publishEvent(any(StrategyStopEvent.class));
        verify(eventPublisher).publishEvent(any(AnalysisEvent.class));
        verify(eventPublisher).publishEvent(any(AccountEvent.class));
        assertFalse(backtestExecutor.isInitialised());
    }

    @Test
    void testUninitialisedCalls() {
        Tick tick = mock(Tick.class);
        Bar bar = mock(Bar.class);
        backtestExecutor.onTick(tick, bar);
        backtestExecutor.onBarClose(bar);
        backtestExecutor.onStop();
        verify(strategy, never()).onTick(any(), any());
        verify(strategy, never()).onBarClose(any());
        verify(strategy, never()).onDeInit();
    }
}