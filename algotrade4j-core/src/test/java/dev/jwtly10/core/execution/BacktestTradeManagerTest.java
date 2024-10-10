package dev.jwtly10.core.execution;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.TradeEvent;
import dev.jwtly10.core.exception.InvalidTradeException;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.risk.RiskManager;
import dev.jwtly10.core.risk.RiskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static dev.jwtly10.core.model.Instrument.NAS100USD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class BacktestTradeManagerTest {
    private final Instrument SYMBOL = NAS100USD;
    private BacktestTradeManager backtestTradeManager;
    private EventPublisher mockEventPublisher;
    private Tick mockCurrentTick;
    private BarSeries mockBarSeries;
    private RiskManager mockRiskManager;

    @BeforeEach
    void setUp() {
        mockEventPublisher = mock(EventPublisher.class);
        mockBarSeries = mock(DefaultBarSeries.class);
        mockCurrentTick = mock(DefaultTick.class);
        mockRiskManager = mock(RiskManager.class);
        backtestTradeManager = new BacktestTradeManager(mockCurrentTick, mockBarSeries, "BacktestTradeManager", mockEventPublisher, mockRiskManager);
    }

    @Test
    void testOpenLongPosition() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("50000"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("50000"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("50000"));
        params.setStopLoss(new Number("49000"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(10000.0);

        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openLong(params).getId();

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getInstrument().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());
    }

    @Test
    void testOpenShortPosition() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("50000"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("50000"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("50000"));
        params.setStopLoss(new Number("49000"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(10000);

        ZonedDateTime openTime = ZonedDateTime.now();
        when(mockCurrentTick.getDateTime()).thenReturn(openTime);
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), openTime, new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openShort(params).getId();


        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getInstrument().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());
        assertEquals(openTime, backtestTradeManager.getOpenTrades().get(tradeId).getOpenTime());
    }

    @Test
    void testOpenLongCalculateTP() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("10"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("10"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("10"));
        params.setStopLoss(new Number("8"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(100);

        ZonedDateTime openTime = ZonedDateTime.now();
        when(mockCurrentTick.getDateTime()).thenReturn(openTime);
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), openTime, new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openLong(params).getId();

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getInstrument().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());
        assertEquals(new Number(14), backtestTradeManager.getOpenTrades().get(tradeId).getTakeProfit());
        assertEquals(0.5, backtestTradeManager.getOpenTrades().get(tradeId).getQuantity());
        assertEquals(openTime, backtestTradeManager.getOpenTrades().get(tradeId).getOpenTime());
    }

    @Test
    void testOpenShortCalculateTP() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("10"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("10"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("10"));
        params.setStopLoss(new Number("12"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(100);

        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openShort(params).getId();

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.OPEN &&
                        ((TradeEvent) event).getInstrument().equals(SYMBOL)
        ));
        assertNotNull(tradeId);
        assertEquals(1, backtestTradeManager.getOpenTrades().size());
        assertEquals(new Number(6), backtestTradeManager.getOpenTrades().get(tradeId).getTakeProfit());
        assertEquals(0.5, backtestTradeManager.getOpenTrades().get(tradeId).getQuantity());
    }

    @Test
    void testClosePositionLongProfit() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("10"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("10"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("10"));
        params.setStopLoss(new Number("8"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(100);
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openLong(params).getId();
        assertEquals(1, backtestTradeManager.getOpenTrades().size());

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, new Number("14"), new Number("12"), new Number("10"), new Number("100"), ZonedDateTime.now()));

        backtestTradeManager.closePosition(tradeId, false);

        assertEquals(2, backtestTradeManager.getTrade(tradeId).getProfit());
        assertEquals(0, backtestTradeManager.getOpenTrades().size());

        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getInstrument().equals(SYMBOL)
        ));
    }

    @Test
    void testClosePositionLongLoss() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("10"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("10"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("10"));
        params.setStopLoss(new Number("8"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(100);
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openLong(params).getId();
        assertEquals(1, backtestTradeManager.getOpenTrades().size());

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, new Number("8"), new Number("7"), new Number("9"), new Number("100"), ZonedDateTime.now()));

        backtestTradeManager.closePosition(tradeId, false);

        assertEquals(-1, backtestTradeManager.getTrade(tradeId).getProfit());
        assertEquals(0, backtestTradeManager.getOpenTrades().size());
        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getInstrument().equals(SYMBOL)
        ));
    }

    @Test
    void testClosePositionShortProfit() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("10"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("10"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("10"));
        params.setStopLoss(new Number("12"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(100);
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openShort(params).getId();
        assertEquals(1, backtestTradeManager.getOpenTrades().size());

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, new Number("6"), new Number("5"), new Number("7"), new Number("100"), ZonedDateTime.now()));

        backtestTradeManager.closePosition(tradeId, false);

        assertEquals(1.5, backtestTradeManager.getTrade(tradeId).getProfit());
        assertEquals(0, backtestTradeManager.getOpenTrades().size());
        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getInstrument().equals(SYMBOL)
        ));
    }

    @Test
    void testClosePositionShortLoss() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("10"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("10"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("10"));
        params.setStopLoss(new Number("12"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(100);
        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openShort(params).getId();
        assertEquals(1, backtestTradeManager.getOpenTrades().size());

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, new Number("12"), new Number("11"), new Number("13"), new Number("100"), ZonedDateTime.now()));

        backtestTradeManager.closePosition(tradeId, false);

        assertEquals(-1.05, backtestTradeManager.getTrade(tradeId).getProfit());
        assertEquals(0, backtestTradeManager.getOpenTrades().size());
        verify(mockEventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof TradeEvent &&
                        ((TradeEvent) event).getAction() == TradeEvent.Action.CLOSE &&
                        ((TradeEvent) event).getInstrument().equals(SYMBOL)
        ));
    }

    @Test
    void testOpenPositionValue() {
        Trade trade1 = new Trade(
                NAS100USD,
                0.5,
                new Number("10"),
                ZonedDateTime.now(),
                new Number("8"),
                new Number("12"),
                true
        );
        trade1.setProfit(10);

        Trade trade2 = new Trade(
                NAS100USD,
                0.5,
                new Number("10"),
                ZonedDateTime.now(),
                new Number("12"),
                new Number("8"),
                false
        );
        trade2.setProfit(20);

        backtestTradeManager.getOpenTrades().put(1, trade1);
        backtestTradeManager.getOpenTrades().put(2, trade2);

        assertEquals(30, backtestTradeManager.getOpenPositionValue(SYMBOL));
    }

    @Test
    void testOpenPositionWithNegativeQuantity() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("50000"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("50000"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("50000"));
        when(mockCurrentTick.getDateTime()).thenReturn(ZonedDateTime.now());
        params.setStopLoss(new Number("49999.99"));
        params.setRiskRatio(2);
        params.setRiskPercentage(0.0000001);
        params.setBalanceToRisk(0.01);
        params.setQuantity(-1);

        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));

        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        assertThrows(InvalidTradeException.class, () -> backtestTradeManager.openLong(params));
    }

    @Test
    void testCloseNonExistentPosition() {
        assertThrows(IllegalArgumentException.class, () -> backtestTradeManager.closePosition(999, false));
    }

    @Test
    void testClosePositionWithNullPrice() {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(new Number("50000"));
        when(mockCurrentTick.getBid()).thenReturn(new Number("50000"));
        when(mockCurrentTick.getAsk()).thenReturn(new Number("50000"));
        params.setStopLoss(new Number("49000"));
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(10000);

        when(mockBarSeries.getLastBar()).thenReturn(new DefaultBar(NAS100USD, Duration.ofDays(1), ZonedDateTime.now(), new Number("100"), new Number("100"), new Number("100"), new Number("100"), new Number("100")));
        when(mockRiskManager.canTrade()).thenReturn(new RiskStatus(false, null));
        int tradeId = backtestTradeManager.openLong(params).getId();

        backtestTradeManager.setCurrentTick(new DefaultTick(SYMBOL, null, null, new Number("10"), new Number("100"), ZonedDateTime.now()));

        assertThrows(IllegalStateException.class, () -> backtestTradeManager.closePosition(tradeId, false));
    }
}