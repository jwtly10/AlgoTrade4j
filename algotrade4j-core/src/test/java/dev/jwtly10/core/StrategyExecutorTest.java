package dev.jwtly10.core;

import dev.jwtly10.core.datafeed.DataFeed;
import dev.jwtly10.core.datafeed.DataFeedException;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.StrategyStopEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

class StrategyExecutorTest {

    @Mock
    private Strategy mockStrategy;
    @Mock
    private PriceFeed mockPriceFeed;
    @Mock
    private BarSeries mockBarSeries;
    @Mock
    private DataFeed mockDataFeed;
    @Mock
    private EventPublisher mockEventPublisher;
    @Mock
    private TradeManager mockTradeManager;
    @Mock
    private Bar mockBar;
    @Mock
    private Account mockAccount;

    private StrategyExecutor strategyExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockStrategy.getStrategyId()).thenReturn("TestStrategy");
        strategyExecutor = new StrategyExecutor(mockStrategy, mockTradeManager, mockPriceFeed, mockBarSeries, mockDataFeed, mockEventPublisher);
    }

    @Test
    void testRun() throws DataFeedException {
        doNothing().when(mockDataFeed).addMarketDataListener(any());
        doNothing().when(mockStrategy).onStart();
        doThrow(new DataFeedException("Test exception")).when(mockDataFeed).start();

        strategyExecutor.run();

        // Assert that when we run the strategy, we call the onStart method, add the bar listener, and start the data feed
        // We also assert that if the dateFeed fails, we stop the data feed and remove the listener
        // and publish a strategy stop event
        verify(mockStrategy).onStart();
        verify(mockDataFeed).removeMarketDataListener(strategyExecutor);
        verify(mockDataFeed).start();
        verify(mockDataFeed).stop();
        verify(mockDataFeed).removeMarketDataListener(strategyExecutor);
        verify(mockStrategy).onDeInit();
        verify(mockEventPublisher).publishEvent(any(StrategyStopEvent.class));
    }

    @Test
    void testOnBar() {
        when(mockBar.getSymbol()).thenReturn("AAPL");
        when(mockBar.getOpenTime()).thenReturn(ZonedDateTime.now());
        when(mockPriceFeed.getBid(anyString())).thenReturn(new Number("10"));
        when(mockPriceFeed.getAsk(anyString())).thenReturn(new Number("10"));
        when(mockTradeManager.getAccount()).thenReturn(mockAccount);

        // We dont process on bar if we are not running
        strategyExecutor.setRunning(true);

        strategyExecutor.onBarClose(mockBar);

        // Assert we call the strategy and publish the new bar event
        verify(mockEventPublisher).publishEvent(any(BarEvent.class));
        verify(mockStrategy).onBarClose(eq(mockBar), eq(mockBarSeries), anyList(), any(TradeManager.class));
    }

    @Test
    void testStop() throws DataFeedException {
        strategyExecutor.stop();

        verify(mockDataFeed).stop();
        verify(mockDataFeed).removeMarketDataListener(strategyExecutor);
    }


    @Test
    void testOnStop() throws DataFeedException {
        strategyExecutor.onStop();

        verify(mockDataFeed).stop();
        verify(mockDataFeed).removeMarketDataListener(strategyExecutor);
    }
}