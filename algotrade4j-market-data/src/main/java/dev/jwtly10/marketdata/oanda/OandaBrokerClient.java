package dev.jwtly10.marketdata.oanda;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.marketdata.common.BrokerClient;
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
    public Account getAccountInfo() {
        if (accountId == null) {
            log.error("Account ID not set. Cannot fetch account info.");
            throw new RuntimeException("Account ID not set. Cannot fetch account info.");
        }
        try {
            OandaAccountResponse res = client.fetchAccount(accountId);
            // TODO: Improve this. I have just set to -999999 to make it clear if we ever try to use this value (we shouldn't)
            return new Account(-999999, Double.parseDouble(res.account().balance()), Double.parseDouble(res.account().nAV()));
        } catch (Exception e) {
            log.error("Error fetching account info", e);
            return null;
        }
    }

    @Override
    public List<Trade> getOpenTrades() {
        if (accountId == null) {
            log.error("Account ID not set. Cannot fetch open trades.");
            throw new RuntimeException("Account ID not set. Cannot fetch open trades.");
        }
        try {
            OandaTradeResponse res = client.fetchTrades(accountId, null, TradeStateFilter.OPEN, null, 500);
            return res.trades().stream().map(OandaTrade::toTrade).toList();
        } catch (Exception e) {
            log.error("Error fetching open trades", e);
            return null;
        }
    }

    @Override
    public List<Trade> getAllTrades() throws Exception {
        if (accountId == null) {
            log.error("Account ID not set. Cannot fetch all trades.");
            throw new RuntimeException("Account ID not set. Cannot fetch all trades.");
        }
        OandaTradeResponse res = client.fetchTrades(accountId, null, TradeStateFilter.ALL, null, 500);
        return res.trades().stream().map(OandaTrade::toTrade).toList();
    }

    @Override
    public Trade openTrade(Trade trade) {
        if (accountId == null) {
            log.error("Account ID not set. Cannot open trade.");
            throw new RuntimeException("Account ID not set. Cannot open trade.");
        }
        try {
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
        } catch (Exception e) {
            log.error("Error opening trade", e);
            return null;
        }
    }

    @Override
    public void closeTrade(Integer tradeId) {
        if (accountId == null) {
            log.error("Account ID not set. Cannot close trade.");
            throw new RuntimeException("Account ID not set. Cannot close trade.");
        }
        try {
            client.closeTrade(accountId, String.valueOf(tradeId));
        } catch (Exception e) {
            log.error("Error closing trade", e);
        }
    }

    @Override
    public void streamPrices(List<Instrument> instruments, Object callback) {
        if (accountId == null) {
            log.error("Account ID not set. Cannot stream prices.");
            throw new RuntimeException("Account ID not set. Cannot stream prices.");
        }
        try {
            // Hack - we don't have any other clients so this works for now.
            OandaClient.PriceStreamCallback c = (OandaClient.PriceStreamCallback) callback;
            client.streamPrices(accountId, instruments, c);
        } catch (Exception e) {
            log.error("Error streaming prices", e);
        }
    }

    @Override
    public void streamTransactions(OandaClient.TransactionStreamCallback callback) throws Exception {
        log.info("Starting transaction stream");
        if (accountId == null) {
            log.error("Account ID not set. Cannot stream transactions.");
            throw new RuntimeException("Account ID not set. Cannot stream transactions.");
        }
        client.streamTransactions(accountId, callback);
    }
}