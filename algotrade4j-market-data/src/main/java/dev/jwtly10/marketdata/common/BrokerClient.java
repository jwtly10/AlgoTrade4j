package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.Broker;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.core.model.TradeParameters;
import dev.jwtly10.marketdata.common.stream.Stream;

import java.util.List;

/**
 * Interface for interacting with a broker client.
 * It is simply a wrapper around the client implementation, to allow for easy switching between different broker clients.
 */
public interface BrokerClient {

    /**
     * Returns the broker enum value associated with the client
     *
     * @return The broker
     */
    Broker getBroker();

    /**
     * Retrieves account information.
     *
     * @return The account information.
     * @throws Exception should account information not be available
     */
    Account getAccountInfo() throws Exception;

    /**
     * Retrieves a list of open trades.
     *
     * @return A list of open trades.
     * @throws Exception If an error occurs while retrieving the trades.
     */
    List<Trade> getOpenTrades() throws Exception;

    /**
     * Retrieves a list of all trades.
     *
     * @return A list of all trades.
     * @throws Exception If an error occurs while retrieving the trades.
     */
    List<Trade> getAllTrades() throws Exception;

    /**
     * Opens a new trade.
     *
     * @param trade The trade to be opened.
     * @return The opened trade.
     * @throws Exception If an error occurs while opening the trade.
     */
    Trade openTrade(Trade trade) throws Exception;

    /**
     * Opens a new trade.
     * Useful for opening trades with brokers where we dont have all the context about the execution.
     *
     * @param tradeParameters The parameters for the trade to be opened.
     * @return The opened trade.
     * @throws Exception If an error occurs while opening the trade.
     */
    Trade openTrade(TradeParameters tradeParameters) throws Exception;

    /**
     * Closes an existing trade.
     *
     * @param tradeId The ID of the trade to be closed.
     * @throws Exception If an error occurs while closing the trade.
     */
    void closeTrade(Integer tradeId) throws Exception;

    /**
     * Streams prices for a list of instruments.
     *
     * @param instruments The list of instruments to stream prices for.
     * @return A stream of prices.
     */
    Stream<?> streamPrices(List<Instrument> instruments);

    /**
     * Streams transactions.
     *
     * @return A stream of transactions.
     */
    Stream<?> streamTransactions();
}