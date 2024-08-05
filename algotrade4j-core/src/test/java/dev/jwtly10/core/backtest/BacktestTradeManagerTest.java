package dev.jwtly10.core.backtest;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.*;
import dev.jwtly10.core.defaults.DefaultBar;
import dev.jwtly10.core.defaults.DefaultBarSeries;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.TradeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class BacktestTradeManagerTest {
    private final String SYMBOL = "BTCUSD";
    private BacktestTradeManager backtestTradeManager;
    private MockPriceFeed mockPriceFeed;
    private EventPublisher mockEventPublisher;
    private Tick currentTick;
    private BarSeries mockBarSeries;

    @BeforeEach
    void setUp() {
        mockPriceFeed = new MockPriceFeed();
        mockEventPublisher = mock(EventPublisher.class);
        mockBarSeries = mock(DefaultBarSeries.class);
        backtestTradeManager = new BacktestTradeManager(currentTick, mockBarSeries, "BacktestTradeManager", mockEventPublisher);
    }

    @Test
    void testOpenLongPosition() {
        mockPriceFeed.setPrice(new Number("99"));
        TradeParameters params = new TradeParameters();
        params.setSymbol(SYMBOL);
        params.setEntryPrice(new Number("50000"));
        params.setStopLoss(new Number("49000"));
        params.setRiskRatio(new Number("2"));
        params.setRiskPercentage(new Number("1"));
        params.setBalanceToRisk(new Number("10000"));

        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar("BTCUSD", Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        String tradeId = backtestTradeManager.openLong(params);

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());
    }

    @Test
    void testOpenShortPosition() {
        mockPriceFeed.setPrice(new Number("99"));
        TradeParameters params = new TradeParameters();
        params.setSymbol(SYMBOL);
        params.setEntryPrice(new Number("50000"));
        params.setStopLoss(new Number("49000"));
        params.setRiskRatio(new Number("2"));
        params.setRiskPercentage(new Number("1"));
        params.setBalanceToRisk(new Number("10000"));

        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar("BTCUSD", Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        String tradeId = backtestTradeManager.openShort(params);


        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());
    }


    // TODO: More tests with different parameter types

//
//    @Test
//    void testClosePositionLongProfit() {
//        mockPriceFeed.setPrice(new Number("100"));
//        String tradeId = backtestTradeManager.openLong(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
//        // Verify that the trade events are published
//        verify(mockEventPublisher).publishEvent(argThat(event ->
//                event instanceof TradeEvent &&
//                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
//                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
//        ));
//
//        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
//        backtestTradeManager.updateTrades(bar);
//        backtestTradeManager.closePosition(tradeId);
//
//        // Verify that the trade events are published
//        verify(mockEventPublisher).publishEvent(argThat(event ->
//                event instanceof TradeEvent &&
//                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
//                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
//        ));
//        verifyNoMoreInteractions(mockEventPublisher);
//        // Equity change
//        // Profit per unit = 107.99 - 100.01 = 7.98 (Current price - Entry price)
//        // Total profit = 7.98 * 10 = 79.8 (Profit per unit * Quantity)
//        assertEquals(new Number("10079.80"), backtestTradeManager.getBalance());
//        assertEquals(new Number("10079.80"), backtestTradeManager.getEquity());
//    }
//
//    @Test
//    void testClosePositionLongLoss() {
//        mockPriceFeed.setPrice(new Number("100"));
//        String tradeId = backtestTradeManager.openLong(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
//        // Verify that the trade events are published
//        verify(mockEventPublisher).publishEvent(argThat(event ->
//                event instanceof TradeEvent &&
//                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
//                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
//        ));
//
//        Bar bar = createBar(new Number("108"), new Number("110"), new Number("100"), new Number("97"));
//        backtestTradeManager.updateTrades(bar);
//        backtestTradeManager.closePosition(tradeId);
//
//        // Verify that the trade events are published
//        verify(mockEventPublisher).publishEvent(argThat(event ->
//                event instanceof TradeEvent &&
//                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
//                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
//        ));
//        verifyNoMoreInteractions(mockEventPublisher);
//        // Equity change
//        // Profit per unit = 96.99 - 100.01 = -3.02 (Current price - Entry price)
//        // Total profit = -3.02 * 10 = -30.20 (Profit per unit * Quantity)
//        assertEquals(new Number("9969.80"), backtestTradeManager.getBalance());
//        assertEquals(new Number("9969.80"), backtestTradeManager.getEquity());
//    }
//
//    @Test
//    void testClosePositionShortProfit() {
//        mockPriceFeed.setPrice(new Number("100"));
//        String tradeId = backtestTradeManager.openShort(SYMBOL, new Number("10"), new Number("110"), new Number("95"));
//        // Verify that the trade events are published
//        verify(mockEventPublisher).publishEvent(argThat(event ->
//                event instanceof TradeEvent &&
//                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
//                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
//        ));
//
//        Bar bar = createBar(new Number("108"), new Number("110"), new Number("100"), new Number("97"));
//        backtestTradeManager.updateTrades(bar);
//        backtestTradeManager.closePosition(tradeId);
//
//        // Verify that the trade events are published
//        verify(mockEventPublisher).publishEvent(argThat(event ->
//                event instanceof TradeEvent &&
//                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
//                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
//        ));
//        // Equity change
//        // Profit per unit = 99.99 - 97.01 = 2.98 (Current price - Entry price)
//        // Total profit = 2.98 * 10 = 29.80 (Profit per unit * Quantity)
//        assertEquals(new Number("10029.80"), backtestTradeManager.getBalance());
//        assertEquals(new Number("10029.80"), backtestTradeManager.getEquity());
//    }
//
//    @Test
//    void testClosePositionShortLoss() {
//        mockPriceFeed.setPrice(new Number("100"));
//        String tradeId = backtestTradeManager.openShort(SYMBOL, new Number("10"), new Number("110"), new Number("95"));
//        Bar bar = createBar(new Number("108"), new Number("110"), new Number("100"), new Number("105"));
//        backtestTradeManager.updateTrades(bar);
//        backtestTradeManager.closePosition(tradeId);
//        // Equity change
//        // Profit per unit = 99.99 - 105.01 = -5.02 (Current price - Entry price)
//        // Total profit = -5.02 * 10 = -50.20 (Profit per unit * Quantity)
//        assertEquals(new Number("9949.80"), backtestTradeManager.getBalance());
//        assertEquals(new Number("9949.80"), backtestTradeManager.getBalance());
//    }
//
//
//    @Test
//    void testGetPosition() {
//        backtestTradeManager.openLong(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
//        backtestTradeManager.openShort(SYMBOL, new Number("5"), new Number("105"), new Number("90"));
//        assertEquals(new Number("5"), backtestTradeManager.getOpenPosition(SYMBOL));
//    }
//
//    @Test
//    void testUpdateTradesStopLoss() {
//        mockPriceFeed.setPrice(new Number("100"));
//        backtestTradeManager.openLong(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
//        Bar bar = createBar(new Number("94"), new Number("96"), new Number("93"), new Number("95"));
//        backtestTradeManager.updateTrades(bar);
//        assertEquals(new Number("9949.80"), backtestTradeManager.getBalance());
//        assertEquals(new Number("9949.80"), backtestTradeManager.getEquity());
//    }
//
//    @Test
//    void testUpdateTradesTakeProfit() {
//        mockPriceFeed.setPrice(new Number("100"));
//        backtestTradeManager.openShort(SYMBOL, new Number("10"), new Number("105"), new Number("95"));
//        Bar bar = createBar(new Number("92"), new Number("93"), new Number("89"), new Number("90"));
//        backtestTradeManager.updateTrades(bar);
//
//        assertEquals(new Number("10099.80"), backtestTradeManager.getBalance());
//        assertEquals(new Number("10099.80"), backtestTradeManager.getEquity());
//    }
//
//    @Test
//    void testUpdateAccountState() {
//        mockPriceFeed.setPrice(new Number("100"));
//        backtestTradeManager.openLong(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
//        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
//        backtestTradeManager.updateTrades(bar);
//        assertEquals(new Number("10000"), backtestTradeManager.getBalance());
//        assertEquals(new Number("79.80"), backtestTradeManager.getOpenPositionValue());
//        assertEquals(new Number("10079.80"), backtestTradeManager.getEquity());
//    }
//
//    @Test
//    void testCloseNonExistentPosition() {
//        assertThrows(IllegalArgumentException.class, () -> backtestTradeManager.closePosition("non-existent-id"));
//    }
//
//    @Test
//    void testClosePositionWithoutCurrentBar() {
//        String tradeId = backtestTradeManager.openLong(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
//        assertThrows(IllegalStateException.class, () -> backtestTradeManager.closePosition(tradeId));
//    }
//
//
//    private Bar createBar(Number open, Number high, Number low, Number close) {
//        mockPriceFeed.setPrice(close);
//        return DefaultBar.builder()
//                .symbol(SYMBOL)
//                .timePeriod(Duration.ofMinutes(1))
//                .dateTime(ZonedDateTime.now())
//                .open(open)
//                .high(high)
//                .low(low)
//                .close(close)
//                .volume(1000)
//                .build();
//    }
//}
}