package dev.jwtly10.core.execution;

import dev.jwtly10.core.exception.InvalidTradeException;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.core.model.TradeParameters;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The TradeManager interface defines the contract for executing trades and managing trading positions.
 * It provides methods for opening and closing positions, retrieving trade information, and updating account status.
 * Implementations of this interface can be used to execute trades in different live trading environments or backtest trading strategies.
 */
public interface TradeManager {
    /**
     * Updates the list of open trades in the trading account.
     * This method is called on each update (for live integrations) to keep the list of all trades up to date.
     * The list of open trades is used to calculate the total value of open positions and to manage stop-loss and take-profit orders.
     *
     * @param trades The list of open trades in the trading account
     */
    void updateOpenTrades(List<Trade> trades);

    /**
     * Updates the list of all trades in the trading account.
     * This method is called on each update (for live integrations) to keep the list of all trades up to date.
     *
     * @param trades The list of all trades in the trading account
     */
    void updateAllTrades(List<Trade> trades);

    /**
     * Opens a long position for the specified instrument, uses the current ask price as the entry price.
     * The quantity of the asset to purchase is calculated based on the risk ratio and the balance (configurable)
     *
     * @param params {@link TradeParameters} Trading params as defined in the TradeParameters class
     * @return A unique identifier for the opened trade. Will return null if the trade was not opened
     */
    Integer openLong(TradeParameters params) throws InvalidTradeException;

    /**
     * Opens a short position for the specified instrument, uses the current bid price as the entry price.
     * The quantity of the asset to purchase is calculated based on the risk ratio and the balance (configurable)
     *
     * @param params {@link TradeParameters} Trading params as defined in the TradeParameters class
     * @return A unique identifier for the opened trade. Will return null if the trade was not opened
     */
    Integer openShort(TradeParameters params) throws InvalidTradeException;

    /**
     * Closes an existing position identified by the trade ID.
     *
     * @param tradeId The unique identifier of the trade to close
     * @param manual  Weather the close was manually triggered or a result of a stoploss/tp (this lets us manually handle slippage)
     */
    void closePosition(Integer tradeId, boolean manual) throws InvalidTradeException;

    /**
     * Loads all trades from the trading account.
     */
    void loadTrades() throws Exception;

    /**
     * Retrieves the details of a specific trade.
     *
     * @param tradeId The unique identifier of the trade
     * @return The Trade object containing the details of the specified trade
     */
    Trade getTrade(Integer tradeId);

    /**
     * Gets the total value of all open positions.
     *
     * @return The total value of open positions
     */
    double getOpenPositionValue(Instrument instrument);

    /**
     * Gets the map of all trades in the trading account.
     *
     * @return The map of all trades
     */
    ConcurrentHashMap<Integer, Trade> getAllTrades();

    /**
     * Gets the map of open trades in the trading account.
     *
     * @return The map of open trades
     */
    ConcurrentHashMap<Integer, Trade> getOpenTrades();

    /**
     * Sets the current tick for the strategy.
     *
     * @param tick The current tick
     */
    void setCurrentTick(Tick tick);

    /**
     * Returns the strategy ID of the current strategy.
     *
     * @return The strategy ID
     */
    String getStrategyId();

    enum BALANCE_TYPE {
        EQUITY,
        BALANCE,
        INITIAL
    }
}