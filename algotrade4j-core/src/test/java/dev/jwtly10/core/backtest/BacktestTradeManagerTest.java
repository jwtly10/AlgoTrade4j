package dev.jwtly10.core.backtest;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.*;
import dev.jwtly10.core.defaults.DefaultBar;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.TradeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class BacktestTradeManagerTest {
    private final String SYMBOL = "AAPL";
    private BacktestTradeManager executor;
    private MockPriceFeed mockPriceFeed;
    private EventPublisher mockEventPublisher;

    @BeforeEach
    void setUp() {
        mockPriceFeed = new MockPriceFeed();
        mockEventPublisher = mock(EventPublisher.class);
        executor = new BacktestTradeManager("BacktestTradeManagerTest", new Number("10000"), mockPriceFeed, mockEventPublisher);
    }

    @Test
    void testOpenLongPosition() {
        // TODO: Spreads can instantly effect equity. This may be ignored for now, but needs to be considered in the future.
        mockPriceFeed.setPrice(new Number("99"));
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("95"), new Number("110"));

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(new Number("0"), executor.getOpenPositionValue());
        assertEquals(new Number("10000"), executor.getBalance());
        assertEquals(new Number("10000"), executor.getEquity());
        assertEquals(new Number("99.01"), executor.getTrade(tradeId).getEntryPrice());
    }

    @Test
    void testOpenShortPosition() {
        mockPriceFeed.setPrice(new Number("100"));
        String tradeId = executor.openShortPosition(SYMBOL, new Number("10"), new Number("105"), new Number("90"));

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(new Number("0"), executor.getOpenPositionValue());
        assertEquals(new Number("10000"), executor.getBalance());
        assertEquals(new Number("10000"), executor.getEquity());
        assertEquals(new Number("99.99"), executor.getTrade(tradeId).getEntryPrice());
    }

    @Test
    void testOpenLongPositionGeneratedRisk() {
        mockPriceFeed.setPrice(new Number("100"));
        Number stopLoss = new Number("98");
        Number riskRatio = new Number("2");
        Number risk = new Number("1"); // 1%
        TradeManager.BALANCE_TYPE balanceType = TradeManager.BALANCE_TYPE.BALANCE;

        String tradeId = executor.openLongPosition(SYMBOL, stopLoss, riskRatio, risk, balanceType);

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(new Number("10000"), executor.getBalance());
        assertEquals(new Number("10000"), executor.getEquity());

        Trade trade = executor.getTrade(tradeId);
        assertNotNull(trade);
        assertEquals(new Number("100.01"), trade.getEntryPrice());
        assertEquals(stopLoss, trade.getStopLoss());

        Number expectedQuantity = new Number("49.75"); // (10000 * 0.01) / (100.01 - 98), rounded down to 2 decimal places
        Number expectedTakeProfit = new Number("104.03"); // 100.01 + (100.01 - 98) * 2

        assertEquals(expectedQuantity, trade.getQuantity());
        assertEquals(expectedTakeProfit, trade.getTakeProfit());

        // The stop loss size should be half as much as take profit
        var stopLossSize = trade.getEntryPrice().subtract(trade.getStopLoss()).abs();
        var takeProfitSize = trade.getTakeProfit().subtract(trade.getEntryPrice()).abs();
        assertEquals(stopLossSize.multiply(new Number("2").getValue()), takeProfitSize);

        assertTrue(trade.isLong());
    }

    @Test
    void testOpenShortPositionGeneratedRisk() {
        mockPriceFeed.setPrice(new Number("100"));
        Number stopLoss = new Number("102");
        Number riskRatio = new Number("2");
        Number risk = new Number("1"); // 1%
        TradeManager.BALANCE_TYPE balanceType = TradeManager.BALANCE_TYPE.BALANCE;

        String tradeId = executor.openShortPosition(SYMBOL, stopLoss, riskRatio, risk, balanceType);

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(new Number("10000"), executor.getBalance());
        assertEquals(new Number("10000"), executor.getEquity());

        Trade trade = executor.getTrade(tradeId);
        assertNotNull(trade);
        assertEquals(new Number("99.99"), trade.getEntryPrice());
        assertEquals(stopLoss, trade.getStopLoss());

        Number expectedQuantity = new Number("49.75"); // (10000 * 0.01) / (102 - 99.99), rounded down to 2 decimal places
        Number expectedTakeProfit = new Number("95.97"); // 99.99 - (102 - 99.99) * 2

        assertEquals(expectedQuantity, trade.getQuantity());
        assertEquals(expectedTakeProfit, trade.getTakeProfit());

        // The stop loss size should be half as much as take profit
        var stopLossSize = trade.getEntryPrice().subtract(trade.getStopLoss()).abs();
        var takeProfitSize = trade.getTakeProfit().subtract(trade.getEntryPrice()).abs();
        assertEquals(stopLossSize.multiply(new Number("2").getValue()), takeProfitSize);

        // Verify that it's a short position
        assertFalse(trade.isLong());
    }

    @Test
    void testClosePositionLongProfit() {
        mockPriceFeed.setPrice(new Number("100"));
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
        // Verify that the trade events are published
        verify(mockEventPublisher).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));

        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);

        // Verify that the trade events are published
        verify(mockEventPublisher).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        verifyNoMoreInteractions(mockEventPublisher);
        // Equity change
        // Profit per unit = 107.99 - 100.01 = 7.98 (Current price - Entry price)
        // Total profit = 7.98 * 10 = 79.8 (Profit per unit * Quantity)
        assertEquals(new Number("10079.80"), executor.getBalance());
        assertEquals(new Number("10079.80"), executor.getEquity());
    }

    @Test
    void testClosePositionLongLoss() {
        mockPriceFeed.setPrice(new Number("100"));
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
        // Verify that the trade events are published
        verify(mockEventPublisher).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));

        Bar bar = createBar(new Number("108"), new Number("110"), new Number("100"), new Number("97"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);

        // Verify that the trade events are published
        verify(mockEventPublisher).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        verifyNoMoreInteractions(mockEventPublisher);
        // Equity change
        // Profit per unit = 96.99 - 100.01 = -3.02 (Current price - Entry price)
        // Total profit = -3.02 * 10 = -30.20 (Profit per unit * Quantity)
        assertEquals(new Number("9969.80"), executor.getBalance());
        assertEquals(new Number("9969.80"), executor.getEquity());
    }

    @Test
    void testClosePositionShortProfit() {
        mockPriceFeed.setPrice(new Number("100"));
        String tradeId = executor.openShortPosition(SYMBOL, new Number("10"), new Number("110"), new Number("95"));
        // Verify that the trade events are published
        verify(mockEventPublisher).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));

        Bar bar = createBar(new Number("108"), new Number("110"), new Number("100"), new Number("97"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);

        // Verify that the trade events are published
        verify(mockEventPublisher).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        // Equity change
        // Profit per unit = 99.99 - 97.01 = 2.98 (Current price - Entry price)
        // Total profit = 2.98 * 10 = 29.80 (Profit per unit * Quantity)
        assertEquals(new Number("10029.80"), executor.getBalance());
        assertEquals(new Number("10029.80"), executor.getEquity());
    }

    @Test
    void testClosePositionShortLoss() {
        mockPriceFeed.setPrice(new Number("100"));
        String tradeId = executor.openShortPosition(SYMBOL, new Number("10"), new Number("110"), new Number("95"));
        Bar bar = createBar(new Number("108"), new Number("110"), new Number("100"), new Number("105"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);
        // Equity change
        // Profit per unit = 99.99 - 105.01 = -5.02 (Current price - Entry price)
        // Total profit = -5.02 * 10 = -50.20 (Profit per unit * Quantity)
        assertEquals(new Number("9949.80"), executor.getBalance());
        assertEquals(new Number("9949.80"), executor.getBalance());
    }


    @Test
    void testGetPosition() {
        executor.openLongPosition(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
        executor.openShortPosition(SYMBOL, new Number("5"), new Number("105"), new Number("90"));
        assertEquals(new Number("5"), executor.getPosition(SYMBOL));
    }

    @Test
    void testUpdateTradesStopLoss() {
        mockPriceFeed.setPrice(new Number("100"));
        executor.openLongPosition(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
        Bar bar = createBar(new Number("94"), new Number("96"), new Number("93"), new Number("95"));
        executor.updateTrades(bar);
        assertEquals(new Number("9949.80"), executor.getBalance());
        assertEquals(new Number("9949.80"), executor.getEquity());
    }

    @Test
    void testUpdateTradesTakeProfit() {
        mockPriceFeed.setPrice(new Number("100"));
        executor.openShortPosition(SYMBOL, new Number("10"), new Number("105"), new Number("95"));
        Bar bar = createBar(new Number("92"), new Number("93"), new Number("89"), new Number("90"));
        executor.updateTrades(bar);

        assertEquals(new Number("10099.80"), executor.getBalance());
        assertEquals(new Number("10099.80"), executor.getEquity());
    }

    @Test
    void testUpdateAccountState() {
        mockPriceFeed.setPrice(new Number("100"));
        executor.openLongPosition(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        assertEquals(new Number("10000"), executor.getBalance());
        assertEquals(new Number("79.80"), executor.getOpenPositionValue());
        assertEquals(new Number("10079.80"), executor.getEquity());
    }

    @Test
    void testCloseNonExistentPosition() {
        assertThrows(IllegalArgumentException.class, () -> executor.closePosition("non-existent-id"));
    }

    @Test
    void testClosePositionWithoutCurrentBar() {
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("95"), new Number("110"));
        assertThrows(IllegalStateException.class, () -> executor.closePosition(tradeId));
    }


    private Bar createBar(Number open, Number high, Number low, Number close) {
        mockPriceFeed.setPrice(close);
        return DefaultBar.builder()
                .symbol(SYMBOL)
                .timePeriod(Duration.ofMinutes(1))
                .dateTime(LocalDateTime.now())
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(1000)
                .build();
    }
}