package dev.jwtly10.core.execution;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.TradeEvent;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class DefaultTradeManagerTest {
    private final String SYMBOL = "BTCUSD";
    private DefaultTradeManager backtestTradeManager;
    private EventPublisher mockEventPublisher;
    private Tick currentTick;
    private BarSeries mockBarSeries;

    @BeforeEach
    void setUp() {
        mockEventPublisher = mock(EventPublisher.class);
        mockBarSeries = mock(DefaultBarSeries.class);
        backtestTradeManager = new DefaultTradeManager(currentTick, mockBarSeries, "BacktestTradeManager", mockEventPublisher);
    }

    @Test
    void testOpenLongPosition() {
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

    @Test
    void testOpenLongCalculateTP() {
        TradeParameters params = new TradeParameters();
        params.setSymbol(SYMBOL);
        params.setEntryPrice(new Number("10"));
        params.setStopLoss(new Number("8"));
        params.setRiskRatio(new Number("2"));
        params.setRiskPercentage(new Number("1"));
        params.setBalanceToRisk(new Number("100"));

        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar("BTCUSD", Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        String tradeId = backtestTradeManager.openLong(params);

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());
        assertEquals(new Number(14), backtestTradeManager.getOpenTrades().get(tradeId).getTakeProfit());
        assertEquals(new Number(0.5), backtestTradeManager.getOpenTrades().get(tradeId).getQuantity());
    }

    @Test
    void testOpenShortCalculateTP() {
        TradeParameters params = new TradeParameters();
        params.setSymbol(SYMBOL);
        params.setEntryPrice(new Number("10"));
        params.setStopLoss(new Number("12"));
        params.setRiskRatio(new Number("2"));
        params.setRiskPercentage(new Number("1"));
        params.setBalanceToRisk(new Number("100"));

        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar("BTCUSD", Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        String tradeId = backtestTradeManager.openShort(params);

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());
        assertEquals(new Number(6), backtestTradeManager.getOpenTrades().get(tradeId).getTakeProfit());
        assertEquals(new Number(0.5), backtestTradeManager.getOpenTrades().get(tradeId).getQuantity());
    }

    @Test
    void testClosePositionLongProfit() {
        TradeParameters params = new TradeParameters();
        params.setSymbol(SYMBOL);
        params.setEntryPrice(new Number("10"));
        params.setStopLoss(new Number("8"));
        params.setRiskRatio(new Number("2"));
        params.setRiskPercentage(new Number("1"));
        params.setBalanceToRisk(new Number("100"));
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar("BTCUSD", Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        String tradeId = backtestTradeManager.openLong(params);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, new Number("14"), new Number("12"), new Number("10"), new Number("100"), ZonedDateTime.now()));

        backtestTradeManager.closePosition(tradeId);

        assertEquals(new Number(2), backtestTradeManager.getTrade(tradeId).getProfit());
        assertEquals(0, backtestTradeManager.getOpenTrades().size());

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
    }

    @Test
    void testClosePositionLongLoss() {
        TradeParameters params = new TradeParameters();
        params.setSymbol(SYMBOL);
        params.setEntryPrice(new Number("10"));
        params.setStopLoss(new Number("8"));
        params.setRiskRatio(new Number("2"));
        params.setRiskPercentage(new Number("1"));
        params.setBalanceToRisk(new Number("100"));
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar("BTCUSD", Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        String tradeId = backtestTradeManager.openLong(params);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, new Number("8"), new Number("7"), new Number("9"), new Number("100"), ZonedDateTime.now()));

        backtestTradeManager.closePosition(tradeId);

        assertEquals(new Number(-1), backtestTradeManager.getTrade(tradeId).getProfit());
        assertEquals(0, backtestTradeManager.getOpenTrades().size());
        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
    }

    @Test
    void testClosePositionShortProfit() {
        TradeParameters params = new TradeParameters();
        params.setSymbol(SYMBOL);
        params.setEntryPrice(new Number("10"));
        params.setStopLoss(new Number("12"));
        params.setRiskRatio(new Number("2"));
        params.setRiskPercentage(new Number("1"));
        params.setBalanceToRisk(new Number("100"));
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar("BTCUSD", Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        String tradeId = backtestTradeManager.openShort(params);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, new Number("6"), new Number("5"), new Number("7"), new Number("100"), ZonedDateTime.now()));

        backtestTradeManager.closePosition(tradeId);

        assertEquals(new Number(1.5), backtestTradeManager.getTrade(tradeId).getProfit());
        assertEquals(0, backtestTradeManager.getOpenTrades().size());
        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
    }

    @Test
    void testClosePositionShortLoss() {
        TradeParameters params = new TradeParameters();
        params.setSymbol(SYMBOL);
        params.setEntryPrice(new Number("10"));
        params.setStopLoss(new Number("12"));
        params.setRiskRatio(new Number("2"));
        params.setRiskPercentage(new Number("1"));
        params.setBalanceToRisk(new Number("100"));
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar("BTCUSD", Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        String tradeId = backtestTradeManager.openShort(params);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, new Number("12"), new Number("11"), new Number("13"), new Number("100"), ZonedDateTime.now()));

        backtestTradeManager.closePosition(tradeId);

        assertEquals(new Number(-1.5), backtestTradeManager.getTrade(tradeId).getProfit());
        assertEquals(0, backtestTradeManager.getOpenTrades().size());
        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getSymbol().equals(SYMBOL)
        ));
    }
}