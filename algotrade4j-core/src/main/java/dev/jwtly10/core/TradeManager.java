package dev.jwtly10.core;

/**
 * The TradeExecutor interface defines the contract for executing trades and managing trading positions.
 * It provides methods for opening and closing positions, retrieving trade information, and updating account status.
 * Implementations of this interface can be used to execute trades in different live trading environments or backtest trading strategies.
 */
public interface TradeManager {

    /**
     * Opens a long position for the specified symbol, uses the current ask price as the entry price.
     *
     * @param symbol     The trading symbol (e.g., stock ticker or currency pair)
     * @param quantity   The quantity of the asset to purchase
     * @param stopLoss   The price at which to exit the position if the trade moves against the position
     * @param takeProfit The price at which to exit the position if the trade moves in favor of the position
     * @return A unique identifier for the opened trade
     */
    String openLongPosition(String symbol, Number quantity, Number stopLoss, Number takeProfit);

    /**
     * Opens a long position for the specified symbol, uses the current ask price as the entry price.
     * The quantity of the asset to purchase is calculated based on the risk ratio and the balance (configurable)
     *
     * @param symbol    The trading symbol (e.g., stock ticker or currency pair)
     * @param stopLoss  The price at which to exit the position if the trade moves against the position
     * @param riskRatio The size of the take profit target relative to the stop loss distance
     * @param risk      The maximum amount of account equity to risk on this trade (in percentage)
     * @return A unique identifier for the opened trade
     */
    String openLongPosition(String symbol, Number stopLoss, Number riskRatio, Number risk, BALANCE_TYPE balanceType);

    /**
     * Opens a short position for the specified symbol, uses the current bid price as the entry price.
     * The quantity of the asset to purchase is calculated based on the risk ratio and the balance (configurable)
     *
     * @param symbol    The trading symbol (e.g., stock ticker or currency pair)
     * @param stopLoss  The price at which to exit the position if the trade moves against the position
     * @param riskRatio The size of the take profit target relative to the stop loss distance
     * @param risk      The maximum amount of account equity to risk on this trade (in percentage)
     * @return A unique identifier for the opened trade
     */
    String openShortPosition(String symbol, Number stopLoss, Number riskRatio, Number risk, BALANCE_TYPE balanceType);

    /**
     * Opens a short position for the specified symbol, uses the current bid price as the entry price.
     *
     * @param symbol     The trading symbol (e.g., stock ticker or currency pair)
     * @param quantity   The quantity of the asset to sell short
     * @param stopLoss   The price at which to exit the position if the trade moves against the position
     * @param takeProfit The price at which to exit the position if the trade moves in favor of the position
     * @return A unique identifier for the opened trade
     */
    String openShortPosition(String symbol, Number quantity, Number stopLoss, Number takeProfit);

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

    /**
     * Retrieves the PriceFeed object associated with this TradeExecutor.
     * Gives strategies option to access the price feed directly
     *
     * @return The PriceFeed object used to retrieve price information
     */
    PriceFeed getPriceFeed();

    enum BALANCE_TYPE {
        EQUITY,
        BALANCE,
        INITIAL
    }
}