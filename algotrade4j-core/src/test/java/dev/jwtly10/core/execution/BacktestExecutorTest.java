package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.*;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
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

    private BacktestExecutor backtestExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(strategy.getStrategyId()).thenReturn("testStrategy");
        backtestExecutor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);
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
        backtestExecutor.onTick(tick, currentBar);
        verify(strategy).onTick(tick, currentBar);
        verify(tradeManager).setCurrentTick(tick);
        verify(tradeStateManager).updateTradeStates(accountManager, tradeManager, tick);
        when(accountManager.getEquity()).thenReturn(new Number(1000));
    }

    @Test
    void testOnBarClose() {
        Bar closedBar = mock(Bar.class);
        backtestExecutor.initialise();
        backtestExecutor.onBarClose(closedBar);
        verify(strategy).onBarClose(closedBar);
    }

    @Test
    void testOnStop() {
        backtestExecutor.initialise();
        when(tradeManager.getOpenTrades()).thenReturn(new ConcurrentHashMap<>());
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