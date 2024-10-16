package dev.jwtly10.core.execution;

import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.model.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The TradeManager interface defines the contract for executing trades and managing trading positions.
 * It provides methods for opening and closing positions, retrieving trade information, and updating account status.
 * Implementations of this interface can be used to execute trades in different live trading environments or backtest trading strategies.
 */
public interface TradeManager {

    Broker getBroker();

    /**
     * Updates the status of open trades
     *
     * @param trades The list of open trades
     */
    void updateOpenTrades(List<Trade> trades);

    /**
     * Updates the status of all trades
     *
     * @param trades The list of all trades
     */
    void updateAllTrades(List<Trade> trades);

    /**
     * Sets the callback to be executed when a trade is closed.
     *
     * @param callback The callback to be executed when a trade is closed
     */
    void setOnTradeCloseCallback(Consumer<Trade> callback);

    /**
     * Opens a long position for the specified instrument, uses the current ask price as the entry price.
     * The quantity of the asset to purchase is calculated based on the risk ratio and the balance (configurable)
     *
     * @param params {@link TradeParameters} Trading params as defined in the TradeParameters class
     * @return A unique identifier for the opened trade
     * @throws Exception If an error occurs while opening the trade
     */
    Trade openLong(TradeParameters params) throws Exception;

    /**
     * Opens a short position for the specified instrument, uses the current bid price as the entry price.
     * The quantity of the asset to purchase is calculated based on the risk ratio and the balance (configurable)
     *
     * @param params {@link TradeParameters} Trading params as defined in the TradeParameters class
     * @return A unique identifier for the opened trade
     * @throws Exception If an error occurs while opening the trade
     */
    Trade openShort(TradeParameters params) throws Exception;

    /**
     * Closes an existing position identified by the trade ID.
     *
     * @param tradeId The unique identifier of the trade to close
     * @param manual  Weather the close was manually triggered or a result of a stoploss/tp (this lets us manually handle slippage)
     * @throws Exception If an error occurs while closing the trade
     */
    void closePosition(Integer tradeId, boolean manual) throws Exception;

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
     * Retrieves all trades.
     *
     * @return A map of all trades, with the trade ID as the key and the Trade object as the value
     */
    Map<Integer, Trade> getAllTrades();

    /**
     * Retrieves all open trades.
     *
     * @return A map of all open trades, with the trade ID as the key and the Trade object as the value
     */
    ConcurrentHashMap<Integer, Trade> getOpenTrades();

    /**
     * Sets the current tick for the TradeManager.
     *
     * @param tick the current tick
     */
    void setCurrentTick(Tick tick);

    /**
     * Starts any background processes.
     */
    void start();

    /**
     * Shuts down any running processes and releases resources.
     * This method should be called when the TradeManager is no longer needed.
     */
    void shutdown();

    /**
     * Utility to have full control when needed during a shutdown
     *
     * @param dataManager The dataManager to be shutdown
     */
    default void setDataManager(DataManager dataManager) {
        // A data manager can be set if needed
    }
}