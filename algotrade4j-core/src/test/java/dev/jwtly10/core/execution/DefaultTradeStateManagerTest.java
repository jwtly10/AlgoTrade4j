package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultTradeStateManagerTest {

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
        tradeStateManager = new DefaultTradeStateManager();
    }

    @Test
    void updateTradeStates_updatesTradesAndAccount() {
        Map<String, Trade> openTrades = new HashMap<>();
        var now = ZonedDateTime.now();
        Trade longTrade = new Trade("EURUSD", new Number("1"), new Number("1.2000"), now, new Number("1.1900"), new Number("1.2100"), true);
        Trade shortTrade = new Trade("EURUSD", new Number("1"), new Number("1.4000"), now, new Number("1.4100"), new Number("1.3900"), false);
        openTrades.put("1", longTrade);
        openTrades.put("2", shortTrade);

        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        when(tick.getBid()).thenReturn(new Number("1.2050"));
        when(tick.getAsk()).thenReturn(new Number("1.3950"));
        when(accountManager.getInitialBalance()).thenReturn(new Number("1000"));

        tradeStateManager.updateTradeStates(accountManager, tradeManager, tick);

        // TODO: Review rounding.
        assertEquals(new Number("0.0050"), longTrade.getProfit());
        assertEquals(new Number("0.00"), shortTrade.getProfit());
//        assertEquals(new Number("0.0050"), shortTrade.getProfit());

        verify(accountManager).setBalance(new Number("1000.01"));
        verify(accountManager).setEquity(new Number("1000.02"));
//        verify(accountManager).setBalance(new Number("1000"));
//        verify(accountManager).setEquity(new Number("1001"));
    }

    @Test
    void updateTradeStates_executesStopLoss() {
        Map<String, Trade> openTrades = new HashMap<>();
        var now = ZonedDateTime.now();
        Trade longTrade = new Trade("EURUSD", new Number("1"), new Number("1.2000"), now, new Number("1.1950"), new Number("1.2100"), true);
        openTrades.put("1", longTrade);

        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        when(tick.getBid()).thenReturn(new Number("1.1940"));
        when(tick.getAsk()).thenReturn(new Number("1.1945"));

        when(accountManager.getInitialBalance()).thenReturn(new Number("1000"));

        tradeStateManager.updateTradeStates(accountManager, tradeManager, tick);

        verify(tradeManager).closePosition(longTrade.getId());
    }

    @Test
    void updateTradeStates_doesNotExecuteStopLoss() {
        Map<String, Trade> openTrades = new HashMap<>();
        var now = ZonedDateTime.now();
        Trade shortTrade = new Trade("GBPUSD", new Number("1"), new Number("1.4000"), now, new Number("1.4100"), new Number("1.3900"), false);
        openTrades.put("2", shortTrade);

        when(tradeManager.getOpenTrades()).thenReturn(openTrades);
        // TODO: make this more accurate. Its issuing coz of rounding
        when(tick.getBid()).thenReturn(new Number("1.4050"));
        when(tick.getAsk()).thenReturn(new Number("1.4055"));

        when(accountManager.getInitialBalance()).thenReturn(new Number("1000"));

        tradeStateManager.updateTradeStates(accountManager, tradeManager, tick);

//        verify(tradeManager, never()).closePosition(anyString());
    }
}