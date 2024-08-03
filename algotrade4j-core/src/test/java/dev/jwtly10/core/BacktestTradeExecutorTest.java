package dev.jwtly10.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BacktestTradeExecutorTest {
    private BacktestTradeExecutor executor;
    private final String SYMBOL = "AAPL";

    @BeforeEach
    void setUp() {
        executor = new BacktestTradeExecutor(new Number("10000"));
    }

    @Test
    void testOpenLongPosition() {
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        assertNotNull(tradeId);
        assertEquals(new Number("0"), executor.getOpenPositionValue());
        assertEquals(new Number("10000"), executor.getBalance());
        assertEquals(new Number("10000"), executor.getEquity());
    }

    @Test
    void testOpenShortPosition() {
        String tradeId = executor.openShortPosition(SYMBOL, new Number("10"), new Number("100"), new Number("105"), new Number("90"));
        assertNotNull(tradeId);
        assertEquals(new Number("0"), executor.getOpenPositionValue());
        assertEquals(new Number("10000"), executor.getBalance());
        assertEquals(new Number("10000"), executor.getEquity());
    }

    @Test
    void testClosePositionLongProfit() {
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);
        // Equity change
        // Profit per unit = 105 - 100 = 5 (Current price - Entry price)
        // Total profit = 5 * 10 = 50 (Profit per unit * Quantity)
        assertEquals(new Number("10050"), executor.getBalance());
    }

    @Test
    void testClosePositionLongLoss() {
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        Bar bar = createBar(new Number("97"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);
        // Equity change
        // Profit per unit = 97 - 100 = -3 (Current price - Entry price)
        // Total profit = -3 * 10 = -30 (Profit per unit * Quantity)
        assertEquals(new Number("9970"), executor.getBalance());
    }

    @Test
    void testClosePositionShortProfit() {
        String tradeId = executor.openShortPosition(SYMBOL, new Number("10"), new Number("100"), new Number("110"), new Number("95"));
        Bar bar = createBar(new Number("97"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);
        // Equity change
        // Profit per unit = 100 - 97 = 3 (Current price - Entry price)
        // Total profit = 3 * 10 = 30 (Profit per unit * Quantity)
        assertEquals(new Number("10030"), executor.getBalance());
    }


    @Test
    void testClosePositionShortLoss() {
        String tradeId = executor.openShortPosition(SYMBOL, new Number("10"), new Number("100"), new Number("110"), new Number("95"));
        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);
        // Equity change
        // Profit per unit = 100 - 105 = -5 (Current price - Entry price)
        // Total profit = -5 * 10 = -50 (Profit per unit * Quantity)
        assertEquals(new Number("9950"), executor.getBalance());
        assertEquals(new Number("9950"), executor.getEquity());
    }


    @Test
    void testGetPosition() {
        executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        executor.openShortPosition(SYMBOL, new Number("5"), new Number("100"), new Number("105"), new Number("90"));
        assertEquals(new Number("5"), executor.getPosition(SYMBOL));
    }

    @Test
    void testUpdateTradesStopLoss() {
        executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        Bar bar = createBar(new Number("94"), new Number("96"), new Number("93"), new Number("95"));
        executor.updateTrades(bar);
        assertEquals(new Number("9940"), executor.getBalance());
    }

    @Test
    void testUpdateTradesTakeProfit() {
        executor.openShortPosition(SYMBOL, new Number("10"), new Number("100"), new Number("105"), new Number("95"));
        Bar bar = createBar(new Number("92"), new Number("93"), new Number("89"), new Number("90"));
        executor.updateTrades(bar);

        assertEquals(new Number("10080"), executor.getBalance());
    }

    @Test
    void testUpdateAccountState() {
        executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        assertEquals(new Number("10000"), executor.getBalance());
        assertEquals(new Number("50"), executor.getOpenPositionValue());
        assertEquals(new Number("10050"), executor.getEquity());
    }

    @Test
    void testCloseNonExistentPosition() {
        assertThrows(IllegalArgumentException.class, () -> executor.closePosition("non-existent-id"));
    }

    @Test
    void testClosePositionWithoutCurrentBar() {
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        assertThrows(IllegalStateException.class, () -> executor.closePosition(tradeId));
    }

    private Bar createBar(Number open, Number high, Number low, Number close) {
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