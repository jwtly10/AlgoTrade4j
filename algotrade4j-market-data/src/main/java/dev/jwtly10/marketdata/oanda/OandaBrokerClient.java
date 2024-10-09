package dev.jwtly10.marketdata.oanda;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.common.TradeDTO;
import dev.jwtly10.marketdata.common.stream.Stream;
import dev.jwtly10.marketdata.oanda.models.OandaTrade;
import dev.jwtly10.marketdata.oanda.models.TradeStateFilter;
import dev.jwtly10.marketdata.oanda.request.MarketOrderRequest;
import dev.jwtly10.marketdata.oanda.response.OandaAccountResponse;
import dev.jwtly10.marketdata.oanda.response.OandaOpenTradeResponse;
import dev.jwtly10.marketdata.oanda.response.OandaTradeResponse;
import dev.jwtly10.marketdata.oanda.utils.OandaUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
public class OandaBrokerClient implements BrokerClient {

    private final OandaClient client;
    private final String accountId;

    public OandaBrokerClient(OandaClient client, String accountId) {
        this.client = client;
        this.accountId = accountId;
    }

    public List<DefaultBar> fetchCandles(Instrument instrument, ZonedDateTime from, ZonedDateTime to, Duration period) throws Exception {
        return OandaUtils.convertOandaCandles(client.fetchCandles(instrument, period, from, to));
    }

    @Override
    public Account getAccountInfo() throws Exception {
        if (accountId == null) {
            log.error("Account ID not set. Cannot fetch account info.");
            throw new RuntimeException("Account ID not set. Cannot fetch account info.");
        }
        OandaAccountResponse res = client.fetchAccount(accountId);
        // TODO: Improve this. I have just set to -999999 to make it clear if we ever try to use this value (we shouldn't)
        return new Account(-999999, Double.parseDouble(res.account().balance()), Double.parseDouble(res.account().nAV()));
    }

    @Override
    public List<Trade> getOpenTrades() throws Exception {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        OandaTradeResponse res = client.fetchTrades(accountId, null, TradeStateFilter.OPEN, null, 500);
        return res.trades().stream().map(OandaTrade::toTrade).toList();
    }

    @Override
    public List<Trade> getAllTrades() throws Exception {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot fetch all trades.");
        }
        OandaTradeResponse res = client.fetchTrades(accountId, null, TradeStateFilter.ALL, null, 500);
        return res.trades().stream().map(OandaTrade::toTrade).toList();
    }

    @Override
    public Trade openTrade(Trade trade) throws Exception {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot open trade.");
        }
        MarketOrderRequest req = MarketOrderRequest.builder()
                .type(MarketOrderRequest.OrderType.MARKET)
                .timeInForce(MarketOrderRequest.TimeInForce.FOK)
                .instrument(trade.getInstrument().getOandaSymbol())
                .units(trade.isLong() ? trade.getQuantity() : -trade.getQuantity()) // Oanda specific logic for determining short/long trades
                .takeProfitOnFill(MarketOrderRequest.TakeProfitDetails.builder()
                        .price(trade.getTakeProfit().getValue().toString())
                        .timeInForce(MarketOrderRequest.TimeInForce.GTC)
                        .build())
                .stopLossOnFill(MarketOrderRequest.StopLossDetails.builder()
                        .price(trade.getStopLoss().getValue().toString())
                        .timeInForce(MarketOrderRequest.TimeInForce.GTC)
                        .build())
                .build();
        OandaOpenTradeResponse res = client.openTrade(accountId, req);

        return new Trade(
                Integer.parseInt(res.orderFillTransaction().orderID()), // external id
                Instrument.fromOandaSymbol(res.orderCreateTransaction().instrument()), // instrument
                trade.isLong() ? trade.getQuantity() : -trade.getQuantity(), // Quantity
                ZonedDateTime.parse(res.orderFillTransaction().time()), // open time
                new Number(res.orderFillTransaction().tradeOpened().price()), // entry price
                new Number(res.orderCreateTransaction().stopLossOnFill().price()), // stop loss
                new Number(res.orderCreateTransaction().takeProfitOnFill().price()), // Take profit
                trade.isLong() // is long
        );
    }

    @Override
    public void closeTrade(Integer tradeId) throws Exception {
        if (accountId == null) {
            log.error("Account ID not set. Cannot close trade.");
            throw new RuntimeException("Account ID not set. Cannot close trade.");
        }

        client.closeTrade(accountId, String.valueOf(tradeId));
    }

    @Override
    public Stream<Tick> streamPrices(List<Instrument> instruments) {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot stream prices.");
        }
        log.info("Starting price stream for accountId: {}", accountId);
        return client.streamPrices(accountId, instruments);
    }

    @Override
    public Stream<List<TradeDTO>> streamTransactions() {
        if (accountId == null) {
            throw new RuntimeException("Account ID not set. Cannot stream transactions.");
        }
        log.info("Starting transaction stream for accountId: {}", accountId);
        return client.streamTransactions(accountId);
    }

}