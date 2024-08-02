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
        assertEquals(new Number("9000"), executor.getBalance());
    }

    @Test
    void testOpenShortPosition() {
        String tradeId = executor.openShortPosition(SYMBOL, new Number("10"), new Number("100"), new Number("105"), new Number("90"));
        assertNotNull(tradeId);
        assertEquals(new Number("11000"), executor.getBalance());
    }

    @Test
    void testClosePosition() {
        String tradeId = executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        executor.closePosition(tradeId);
        assertEquals(new Number("10050"), executor.getBalance());
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
        executor.openShortPosition(SYMBOL, new Number("10"), new Number("100"), new Number("105"), new Number("90"));
        Bar bar = createBar(new Number("92"), new Number("93"), new Number("89"), new Number("90"));
        executor.updateTrades(bar);
        assertEquals(new Number("11080"), executor.getBalance());
    }

    @Test
    void testUpdateAccountState() {
        executor.openLongPosition(SYMBOL, new Number("10"), new Number("100"), new Number("95"), new Number("110"));
        Bar bar = createBar(new Number("105"), new Number("110"), new Number("100"), new Number("108"));
        executor.updateTrades(bar);
        assertEquals(new Number("9000"), executor.getBalance());
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