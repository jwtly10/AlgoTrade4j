package dev.jwtly10.core.model;

import dev.jwtly10.core.exception.InvalidTradeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TradeParametersTest {

    private TradeParameters tradeParameters;
    private Instrument instrument;

    @BeforeEach
    void setUp() {
        tradeParameters = new TradeParameters();
        instrument = Instrument.NAS100USD;
    }

    @Test
    void testUseRaw_AllParametersSet_ReturnsTrue() {
        tradeParameters.setEntryPrice(new Number(1.0));
        tradeParameters.setStopLoss(new Number(0.9));
        tradeParameters.setTakeProfit(new Number(1.1));
        tradeParameters.setQuantity(1000);
        tradeParameters.setOpenTime(ZonedDateTime.now());

        assertTrue(tradeParameters.useRaw());
    }

    @Test
    void testUseRaw_MissingParameters_ReturnsFalse() {
        tradeParameters.setEntryPrice(new Number(1.0));
        tradeParameters.setStopLoss(new Number(0.9));
        // Missing takeProfit and quantity

        assertFalse(tradeParameters.useRaw());
    }

    @Test
    void testCreateTrade_RawParameters_Success() {
        tradeParameters.setInstrument(instrument);
        tradeParameters.setEntryPrice(new Number(1.0));
        tradeParameters.setStopLoss(new Number(0.9));
        tradeParameters.setTakeProfit(new Number(1.1));
        tradeParameters.setQuantity(1000);
        tradeParameters.setOpenTime(ZonedDateTime.now());
        tradeParameters.setLong(true);

        Trade trade = tradeParameters.createTrade();

        assertNotNull(trade);
        assertEquals(1000, trade.getQuantity());
        assertEquals(new Number(1.0), trade.getEntryPrice());
        assertEquals(new Number(0.9), trade.getStopLoss());
        assertEquals(new Number(1.1), trade.getTakeProfit());
        assertTrue(trade.isLong());
    }

    @Test
    void testCreateTrade_CalculatedParameters_Success() {
        tradeParameters.setInstrument(instrument);
        tradeParameters.setEntryPrice(new Number(1.0));
        tradeParameters.setStopLoss(new Number(0.9));
        tradeParameters.setRiskPercentage(1); // 1%
        tradeParameters.setRiskRatio(2); // 1:2 risk:reward
        tradeParameters.setBalanceToRisk(10000);
        tradeParameters.setLong(true);
        tradeParameters.setOpenTime(ZonedDateTime.now());

        Trade trade = tradeParameters.createTrade();

        assertNotNull(trade);
        assertEquals(1000, trade.getQuantity()); // 10000 * 0.01 / (1.0 - 0.9) = 1000
        assertEquals(new Number(1.0), trade.getEntryPrice());
        assertEquals(new Number(0.9), trade.getStopLoss());
        assertEquals(new Number(1.2), trade.getTakeProfit()); // 1.0 + (1.0 - 0.9) * 2 = 1.2
        assertTrue(trade.isLong());
    }

    @Test
    void testCreateTrade_CalculatedParameters_ShortTrade_Success() {
        tradeParameters.setInstrument(instrument);
        tradeParameters.setEntryPrice(new Number(1.0));
        tradeParameters.setStopLoss(new Number(1.1));
        tradeParameters.setRiskPercentage(1); // 1%
        tradeParameters.setRiskRatio(2); // 1:2 risk:reward
        tradeParameters.setBalanceToRisk(10000);
        tradeParameters.setLong(false);
        tradeParameters.setOpenTime(ZonedDateTime.now());

        Trade trade = tradeParameters.createTrade();

        assertNotNull(trade);
        assertEquals(1000, trade.getQuantity()); // 10000 * 0.01 / (1.1 - 1.0) = 1000
        assertEquals(new Number(1.0), trade.getEntryPrice());
        assertEquals(new Number(1.1), trade.getStopLoss());
        assertEquals(new Number(0.8), trade.getTakeProfit()); // 1.0 - (1.1 - 1.0) * 2 = 0.8
        assertFalse(trade.isLong());
    }

    @Test
    void testCreateTrade_NegativeQuantity_ThrowsException() {
        tradeParameters.setInstrument(instrument);
        tradeParameters.setEntryPrice(new Number(1.0));
        tradeParameters.setStopLoss(new Number(0.9));
        tradeParameters.setTakeProfit(new Number(1.1));
        tradeParameters.setQuantity(-1000);
        tradeParameters.setOpenTime(ZonedDateTime.now());

        assertThrows(InvalidTradeException.class, () -> tradeParameters.createTrade());
    }

    @Test
    void testCreateTrade_NegativeStopLoss_ThrowsException() {
        tradeParameters.setInstrument(instrument);
        tradeParameters.setEntryPrice(new Number(1.0));
        tradeParameters.setStopLoss(new Number(-0.1));
        tradeParameters.setRiskPercentage(1);
        tradeParameters.setRiskRatio(2);
        tradeParameters.setBalanceToRisk(10000);
        tradeParameters.setLong(true);
        tradeParameters.setOpenTime(ZonedDateTime.now());

        assertThrows(InvalidTradeException.class, () -> tradeParameters.createTrade());
    }
}