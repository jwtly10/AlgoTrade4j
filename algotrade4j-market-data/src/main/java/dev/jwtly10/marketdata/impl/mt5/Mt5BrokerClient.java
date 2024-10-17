package dev.jwtly10.marketdata.impl.mt5;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.common.stream.Stream;
import dev.jwtly10.marketdata.impl.mt5.models.Mt5Trade;
import dev.jwtly10.marketdata.impl.mt5.request.Mt5TradeRequest;
import dev.jwtly10.marketdata.impl.mt5.response.Mt5AccountResponse;
import dev.jwtly10.marketdata.impl.mt5.response.Mt5TradesResponse;
import dev.jwtly10.marketdata.impl.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Mt5BrokerClient implements BrokerClient {
    private final Mt5Client client;
    private final OandaClient oandaClient; // For market data
    private final String accountId;
    private final Broker BROKER;
    private final String defaultOandaAccountId;

    public Mt5BrokerClient(Mt5Client mt5Client, OandaClient oandaClient, String mt5AccountId, String defaultOandaAccountId, Broker broker) {
        this.client = mt5Client;
        this.accountId = mt5AccountId;
        this.oandaClient = oandaClient;
        this.defaultOandaAccountId = defaultOandaAccountId;
        this.BROKER = broker;
    }

    @Override
    public Broker getBroker() {
        return BROKER;
    }

    @Override
    public Account getAccountInfo() throws Exception {
        if (accountId == null) {
            log.error("Account ID not set. Cannot fetch account info.");
            throw new RuntimeException("Account ID not set. Cannot fetch account info.");
        }
        Mt5AccountResponse res = client.fetchAccount(accountId);
        return new Account(-999999, res.equity(), res.balance());
    }

    @Override
    public List<Trade> getOpenTrades() throws Exception {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        Mt5TradesResponse res = client.fetchTrades(accountId);
        List<Trade> allTrades = res.trades().stream().map(
                t -> t.toTrade(BROKER)
        ).toList();
        return allTrades.stream().filter(trade -> trade.getClosePrice() == Number.ZERO).toList();

    }

    @Override
    public List<Trade> getAllTrades() throws Exception {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        Mt5TradesResponse res = client.fetchTrades(accountId);
        return res.trades().stream().map(
                t -> t.toTrade(BROKER)
        ).toList();
    }

    @Override
    public Trade openTrade(Trade trade) throws Exception {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        throw new Exception("Not supported for MT5 Clients. Use openTrade(TradeParameters) instead.");
    }

    @Override
    public Trade openTrade(TradeParameters tradeParameters) throws Exception {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        Mt5Trade res = client.openTrade(accountId, new Mt5TradeRequest(
                tradeParameters.getInstrument().getBrokerConfig(BROKER).getSymbol(),
                tradeParameters.getQuantity(),
                tradeParameters.getEntryPrice(),
                tradeParameters.getStopLoss(),
                tradeParameters.getTakeProfit(),
                tradeParameters.getRiskPercentage(),
                tradeParameters.getRiskRatio(),
                tradeParameters.getBalanceToRisk(),
                tradeParameters.isLong(),
                tradeParameters.getOpenTime().toEpochSecond()
        ));

        return res.toTrade(BROKER);
    }

    @Override
    public void closeTrade(Integer tradeId) throws Exception {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }

        client.closeTrade(accountId, tradeId);
    }

    @Override
    public Stream<?> streamPrices(List<Instrument> instruments) {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot stream prices.");
        }

        if (oandaClient == null) {
            throw new RuntimeException("Oanda client not set. Cannot stream prices.");
        }
        log.info("Starting price stream for accountId: {}", accountId);
        return oandaClient.streamPrices(defaultOandaAccountId, instruments);
    }

    @Override
    public Stream<?> streamTransactions() {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        throw new RuntimeException("Not implemented yet");
    }
}