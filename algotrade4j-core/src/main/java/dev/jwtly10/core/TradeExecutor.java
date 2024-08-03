package dev.jwtly10.core;

/**
 * The TradeExecutor interface defines the contract for executing trades and managing trading positions.
 * It provides methods for opening and closing positions, retrieving trade information, and updating account status.
 */
public interface TradeExecutor {

    /**
     * Opens a long position for the specified symbol.
     *
     * @param symbol     The trading symbol (e.g., stock ticker or currency pair)
     * @param quantity   The quantity of the asset to purchase
     * @param entryPrice The price at which to enter the position
     * @param stopLoss   The price at which to exit the position if the trade moves against the position
     * @param takeProfit The price at which to exit the position if the trade moves in favor of the position
     * @return A unique identifier for the opened trade
     */
    String openLongPosition(String symbol, Number quantity, Number entryPrice, Number stopLoss, Number takeProfit);

    /**
     * Opens a short position for the specified symbol.
     *
     * @param symbol     The trading symbol (e.g., stock ticker or currency pair)
     * @param quantity   The quantity of the asset to sell short
     * @param entryPrice The price at which to enter the position
     * @param stopLoss   The price at which to exit the position if the trade moves against the position
     * @param takeProfit The price at which to exit the position if the trade moves in favor of the position
     * @return A unique identifier for the opened trade
     */
    String openShortPosition(String symbol, Number quantity, Number entryPrice, Number stopLoss, Number takeProfit);

    /**
     * Closes an existing position identified by the trade ID.
     *
     * @param tradeId The unique identifier of the trade to close
     */
    void closePosition(String tradeId);

    /**
     * Retrieves the details of a specific trade.
     *
     * @param tradeId The unique identifier of the trade
     * @return The Trade object containing the details of the specified trade
     */
    Trade getTrade(String tradeId);

    /**
     * Gets the current position size for a given symbol.
     *
     * @param symbol The trading symbol to check
     * @return The current position size (positive for long, negative for short)
     */
    Number getPosition(String symbol);

    /**
     * Retrieves the current equity of the trading account.
     *
     * @return The current equity value
     */
    Number getEquity();

    /**
     * Gets the total value of all open positions.
     *
     * @return The total value of open positions
     */
    Number getOpenPositionValue();

    /**
     * Updates the status of all trades based on the latest price bar.
     *
     * @param bar The latest price bar containing updated market information
     */
    void updateTrades(Bar bar);

    /**
     * Retrieves the current balance of the trading account.
     *
     * @return The current account balance
     */
    Number getBalance();

    /**
     * Gets the Account object associated with this TradeExecutor.
     *
     * @return The Account object containing detailed account information
     */
    Account getAccount();
}