package dev.jwtly10.core;

import java.util.Map;

/**
 * The TradeExecutor interface defines the contract for executing trades and managing trading positions.
 * It provides methods for opening and closing positions, retrieving trade information, and updating account status.
 * Implementations of this interface can be used to execute trades in different live trading environments or backtest trading strategies.
 */
public interface TradeManager {

    /**
     * Opens a long position for the specified symbol, uses the current ask price as the entry price.
     * The quantity of the asset to purchase is calculated based on the risk ratio and the balance (configurable)
     *
     * @param params {@link TradeParameters} Trading params as defined in the TradeParameters class
     * @return A unique identifier for the opened trade
     */
    String openLong(TradeParameters params);

    /**
     * Opens a short position for the specified symbol, uses the current bid price as the entry price.
     * The quantity of the asset to purchase is calculated based on the risk ratio and the balance (configurable)
     *
     * @param params {@link TradeParameters} Trading params as defined in the TradeParameters class
     * @return A unique identifier for the opened trade
     */
    String openShort(TradeParameters params);

    /**
     * Closes an existing position identified by the trade ID.
     *
     * @param tradeId The unique identifier of the trade to close
     */
    void closePosition(String tradeId);

    /**
     * Loads all trades from the trading account.
     */
    void loadTrades();

    /**
     * Retrieves the details of a specific trade.
     *
     * @param tradeId The unique identifier of the trade
     * @return The Trade object containing the details of the specified trade
     */
    Trade getTrade(String tradeId);

    /**
     * Gets the total value of all open positions.
     *
     * @return The total value of open positions
     */
    Number getOpenPositionValue(String symbol);

    Map<String, Trade> getAllTrades();

    Map<String, Trade> getOpenTrades();

    enum BALANCE_TYPE {
        EQUITY,
        BALANCE,
        INITIAL
    }
}