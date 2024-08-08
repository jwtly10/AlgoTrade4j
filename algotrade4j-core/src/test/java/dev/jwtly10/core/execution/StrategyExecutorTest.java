package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.StrategyStopEvent;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class StrategyExecutorTest {

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

    private StrategyExecutor strategyExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(strategy.getStrategyId()).thenReturn("testStrategy");
        strategyExecutor = new StrategyExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher);
    }

    @Test
    void testRun() throws Exception {
        doNothing().when(strategy).onStart();
        doNothing().when(dataManager).addDataListener(any());

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        executor.submit(() -> {
            try {
                strategyExecutor.run();
            } catch (Exception e) {
                fail("Exception should not be thrown");
            }
        });

        TimeUnit.MILLISECONDS.sleep(500); // Give some time for the executor to start
        verify(strategy).onStart();
        verify(dataManager).addDataListener(strategyExecutor);
        verify(dataManager).start();

        strategyExecutor.stop();
        executor.shutdownNow();
    }

    @Test
    void testOnTick() {
        Tick tick = new DefaultTick("AAPL", new Number("150.0"), new Number("150.5"), new Number("149.5"), new Number("150.0"), ZonedDateTime.now());
        Bar currentBar = new DefaultBar("AAPL", Duration.ofDays(1), ZonedDateTime.now(), new Number("150.0"), new Number("152.0"), new Number("149.0"), new Number("151.5"), new Number("2000"));

        strategyExecutor.setRunning(true);
        strategyExecutor.onTick(tick, currentBar);

        verify(eventPublisher).publishEvent(any(BarEvent.class));
        verify(strategy).onTick(tick, currentBar);
        verify(tradeManager).setCurrentTick(tick);
        verify(tradeStateManager).updateTradeStates(accountManager, tradeManager, tick);
    }

    @Test
    void testOnBarClose() {
        Bar closedBar = new DefaultBar("AAPL", Duration.ofDays(1), ZonedDateTime.now(), new Number("150.0"), new Number("152.0"), new Number("149.0"), new Number("151.5"), new Number("2000"));
        when(accountManager.getBalance()).thenReturn(new Number("10000.0"));
        when(accountManager.getEquity()).thenReturn(new Number("10000.0"));

        strategyExecutor.setRunning(true);
        strategyExecutor.onBarClose(closedBar);

        verify(strategy).onBarClose(closedBar);
    }

    @Test
    void testStop() throws Exception {
        when(dataManager.isRunning()).thenReturn(true);

        strategyExecutor.stop();

        verify(dataManager).stop();
        verify(strategy).onDeInit();
        verify(eventPublisher).publishEvent(any(StrategyStopEvent.class));
        assertFalse(strategyExecutor.isRunning());
    }

    @Test
    void testOnStop() {
        strategyExecutor.onStop();

        assertFalse(strategyExecutor.isRunning());
        verify(strategy).onDeInit();
        verify(eventPublisher).publishEvent(any(StrategyStopEvent.class));
    }
}