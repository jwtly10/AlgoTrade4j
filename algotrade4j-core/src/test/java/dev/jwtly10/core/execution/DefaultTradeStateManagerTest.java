package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.event.AccountEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.TradeEvent;
import dev.jwtly10.core.exception.RiskException;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static dev.jwtly10.core.model.Instrument.NAS100USD;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DefaultTradeStateManagerTest {

    @Mock
    private EventPublisher mockEventPublisher;
    @Mock
    private AccountManager accountManager;
    @Mock
    private TradeManager tradeManager;
    @Mock
    private Tick tick;

    private DefaultTradeStateManager tradeStateManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tradeStateManager = new DefaultTradeStateManager("Test", mockEventPublisher);
    }

    @Test
    void updateTradeProfitStateOnTick_updatesTrades() {
        ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
        var now = ZonedDateTime.now();
        Trade longTrade = new Trade(NAS100USD, 1, new Number("1.2000"), now, new Number("1.1900"), new Number("1.2100"), true);
        Trade shortTrade = new Trade(NAS100USD, 1, new Number("1.4000"), now, new Number("1.4100"), new Number("1.3900"), false);
        openTrades.put(1, longTrade);
        openTrades.put(2, shortTrade);

        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        when(tick.getBid()).thenReturn(new Number("1.2050"));
        when(tick.getAsk()).thenReturn(new Number("1.3950"));

        tradeStateManager.updateTradeProfitStateOnTick(tradeManager, tick);
        verify(mockEventPublisher, times(2)).publishEvent(
                any(TradeEvent.class)
        );
    }

    @Test
    void updateAccountState_updatesAccount() {
        ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
        var now = ZonedDateTime.now();
        Trade longTrade = new Trade(NAS100USD, 1, new Number("1.2000"), now, new Number("1.1900"), new Number("1.2100"), true);
        longTrade.setProfit(10);
        Trade shortTrade = new Trade(NAS100USD, 1, new Number("1.4000"), now, new Number("1.4100"), new Number("1.3900"), false);
        shortTrade.setProfit(20);
        Trade shortTrade2 = new Trade(NAS100USD, 1, new Number("1.4000"), now, new Number("1.4100"), new Number("1.3900"), false);
        shortTrade2.setProfit(-15);
        openTrades.put(1, longTrade);
        openTrades.put(2, shortTrade);

        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        when(tick.getBid()).thenReturn(new Number("1.2050"));
        when(tick.getAsk()).thenReturn(new Number("1.3950"));

        when(accountManager.getInitialBalance()).thenReturn(1000.00);
        when(accountManager.getEquity()).thenReturn(1000.00);
        when(accountManager.getBalance()).thenReturn(1000.00);

        tradeStateManager.updateAccountEquityOnTick(accountManager, tradeManager);
        verify(mockEventPublisher, times(1)).publishEvent(
                any(AccountEvent.class)
        );
    }

    @Test
    void updateAccountEquityOnTick_updatesEquity() {
        ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
        var now = ZonedDateTime.now();
        Trade shortTrade = new Trade(NAS100USD, 1, new Number("1.4000"), now, new Number("1.4100"), new Number("1.3900"), false);
        shortTrade.setProfit(100);
        openTrades.put(2, shortTrade);
        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        when(accountManager.getInitialBalance()).thenReturn(1000.0);
        when(accountManager.getBalance()).thenReturn(1000.0);
        when(accountManager.getEquity()).thenReturn(1000.0);

        tradeStateManager.updateAccountEquityOnTick(accountManager, tradeManager);

        verify(accountManager).setEquity(1100.0);
    }


    @Test
    void updateTradeProfitStateOnTick_executesStopLoss() {
        ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
        var now = ZonedDateTime.now();
        Trade longTrade = new Trade(NAS100USD, 1, new Number("1.2000"), now, new Number("1.1950"), new Number("1.2100"), true);
        openTrades.put(1, longTrade);

        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        when(tick.getBid()).thenReturn(new Number("1.1940"));
        when(tick.getAsk()).thenReturn(new Number("1.1945"));

        when(accountManager.getInitialBalance()).thenReturn(1000.0);
        when(accountManager.getEquity()).thenReturn(1000.0);

        tradeStateManager.updateTradeProfitStateOnTick(tradeManager, tick);

        verify(tradeManager).closePosition(longTrade.getId(), false);
    }

    @Test
    void updateTradeProfitStateOnTick_doesNotExecuteStopLoss() {
        ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
        var now = ZonedDateTime.now();
        Trade shortTrade = new Trade(NAS100USD, 1, new Number("1.4000"), now, new Number("1.4100"), new Number("1.3900"), false);
        openTrades.put(2, shortTrade);

        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        // TODO: make this more accurate. Its issuing coz of rounding
        when(tick.getBid()).thenReturn(new Number("1.4050"));
        when(tick.getAsk()).thenReturn(new Number("1.4055"));

        when(accountManager.getInitialBalance()).thenReturn(1000.0);
        when(accountManager.getEquity()).thenReturn(1000.0);

        tradeStateManager.updateTradeProfitStateOnTick(tradeManager, tick);
    }

    @Test
    void updateAccountEquityOnTick_throwsWhenBelowThreshold() {
        ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
        var now = ZonedDateTime.now();
        Trade shortTrade = new Trade(NAS100USD, 1, new Number("1.4000"), now, new Number("1.4100"), new Number("1.3900"), false);
        openTrades.put(2, shortTrade);
        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        when(accountManager.getInitialBalance()).thenReturn(1000.0);
        when(accountManager.getEquity()).thenReturn(10.0); // 1% of equity

        assertThrows(RiskException.class, () -> tradeStateManager.updateAccountEquityOnTick(accountManager, tradeManager));
    }
}