package dev.jwtly10.marketdata.impl.mt5;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.common.TradeDTO;
import dev.jwtly10.marketdata.common.stream.Stream;
import dev.jwtly10.marketdata.impl.mt5.models.MT5Login;
import dev.jwtly10.marketdata.impl.mt5.models.MT5Trade;
import dev.jwtly10.marketdata.impl.mt5.request.MT5TradeRequest;
import dev.jwtly10.marketdata.impl.mt5.response.MT5AccountResponse;
import dev.jwtly10.marketdata.impl.mt5.response.MT5TradesResponse;
import dev.jwtly10.marketdata.impl.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
public class MT5BrokerClient implements BrokerClient {
    private final MT5Client client;
    private final OandaClient oandaClient; // For market data
    private final MT5Login loginDetails;
    private final Broker BROKER;
    private final String defaultOandaAccountId;
    private final ZoneId accountZoneId;

    public MT5BrokerClient(MT5Client mt5Client, OandaClient oandaClient, MT5Login loginDetails, String defaultOandaAccountId, Broker broker, ZoneId zoneId) {
        this.client = mt5Client;
        this.loginDetails = loginDetails;
        this.oandaClient = oandaClient;
        this.defaultOandaAccountId = defaultOandaAccountId;
        this.BROKER = broker;
        this.accountZoneId = zoneId;

        // On obj creation we initialise the account on the server
        try {
            client.initializeAccount(loginDetails.accountId(), loginDetails.password(), loginDetails.server(), loginDetails.path());
        } catch (Exception e) {
            log.error("Error fetching account info: {}", e.getMessage(), e);
            throw new RuntimeException("Error initialising MT5 Account: " + e.getMessage());
        }
    }

    @Override
    public Broker getBroker() {
        return BROKER;
    }

    @Override
    public Account getAccountInfo() throws Exception {
        if (loginDetails.accountId() == null) {
            log.error("Account ID not set. Cannot fetch account info.");
            throw new RuntimeException("Account ID not set. Cannot fetch account info.");
        }
        MT5AccountResponse res = client.fetchAccount(String.valueOf(loginDetails.accountId()));
        return new Account(-999999, res.equity(), res.balance());
    }

    @Override
    public List<Trade> getOpenTrades() throws Exception {
        if (loginDetails.accountId() == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        MT5TradesResponse res = client.fetchTrades(String.valueOf(loginDetails.accountId()));
        List<Trade> allTrades = res.trades().stream().map(
                t -> t.toTrade(BROKER, accountZoneId)
        ).toList();
        return allTrades.stream().filter(trade -> trade.getClosePrice() == Number.ZERO).toList();

    }

    @Override
    public List<Trade> getAllTrades() throws Exception {
        if (loginDetails.accountId() == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        MT5TradesResponse res = client.fetchTrades(String.valueOf(loginDetails.accountId()));
        return res.trades().stream().map(
                t -> t.toTrade(BROKER, accountZoneId)
        ).toList();
    }

    @Override
    public Trade openTrade(Trade trade) throws Exception {
        if (loginDetails.accountId() == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        throw new Exception("Not supported for MT5 Clients. Use openTrade(TradeParameters) instead.");
    }

    @Override
    public Trade openTrade(TradeParameters tradeParameters) throws Exception {
        if (loginDetails.accountId() == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }

        var entryPrice = tradeParameters.getEntryPrice() == null ? null : tradeParameters.getEntryPrice().doubleValue();
        var stopLoss = tradeParameters.getStopLoss() == null ? null : tradeParameters.getStopLoss().doubleValue();
        var takeProfit = tradeParameters.getTakeProfit() == null ? null : tradeParameters.getTakeProfit().doubleValue();

        var tradeDTO = new MT5TradeRequest(
                tradeParameters.getInstrument().getBrokerConfig(BROKER).getSymbol(),
                tradeParameters.getQuantity(),
                entryPrice,
                stopLoss,
                takeProfit,
                tradeParameters.getRiskPercentage(),
                tradeParameters.getRiskRatio(),
                tradeParameters.getBalanceToRisk(),
                tradeParameters.isLong(),
                ZonedDateTime.now().toEpochSecond()
        );

        MT5Trade res = client.openTrade(String.valueOf(loginDetails.accountId()), tradeDTO);

        return res.toTrade(BROKER, accountZoneId);
    }

    @Override
    public void closeTrade(Integer tradeId) throws Exception {
        if (loginDetails.accountId() == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }

        client.closeTrade(String.valueOf(loginDetails.accountId()), tradeId);
    }

    @Override
    public Stream<?> streamPrices(List<Instrument> instruments) {
        if (loginDetails.accountId() == null) {
            throw new RuntimeException("Account ID not set. Cannot stream prices.");
        }

        if (oandaClient == null) {
            throw new RuntimeException("Oanda client not set. Cannot stream prices. MT5 Doesnt support price streams");
        }

        log.info("Starting price stream for mt5 accountId: {} via Oanda", loginDetails.accountId());
        return oandaClient.streamPrices(defaultOandaAccountId, instruments);
    }

    @Override
    public Stream<List<TradeDTO>> streamTransactions() {
        if (loginDetails.accountId() == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        log.info("Starting transaction stream for MT5 accountId: {}", loginDetails.accountId());
        return client.streamTransactions(String.valueOf(loginDetails.accountId()));
    }
}