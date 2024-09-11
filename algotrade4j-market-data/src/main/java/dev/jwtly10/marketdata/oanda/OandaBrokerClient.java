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

    public OandaBrokerClient(OandaClient client) {
        this.client = client;
    }

    public List<DefaultBar> fetchCandles(Instrument instrument, ZonedDateTime from, ZonedDateTime to, Duration period) throws Exception {
        return OandaUtils.convertOandaCandles(client.fetchCandles(instrument, period, from, to));
    }

    @Override
    public Account getAccountInfo() {
        try {
            OandaAccountResponse res = client.fetchAccount();
            // TODO: Make initial balance settable at account level
            return new Account(100000, Double.parseDouble(res.account().balance()), Double.parseDouble(res.account().balance()));
        } catch (Exception e) {
            log.error("Error fetching account info", e);
            return null;
        }
    }

    @Override
    public List<Trade> getOpenTrades() {
        try {
            OandaTradeResponse res = client.fetchTrades(null, TradeStateFilter.OPEN, null, null);
            return res.trades().stream().map(OandaTrade::toTrade).toList();
        } catch (Exception e) {
            log.error("Error fetching open trades", e);
            return null;
        }
    }

    @Override
    public List<Trade> getAllTrades() {
        try {
            OandaTradeResponse res = client.fetchTrades(null, TradeStateFilter.ALL, null, null);
            return res.trades().stream().map(OandaTrade::toTrade).toList();
        } catch (Exception e) {
            log.error("Error fetching all trades", e);
            return null;
        }
    }

    @Override
    public Trade openTrade(Trade trade) {
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
            OandaOpenTradeResponse res = client.openTrade(req);

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
        try {
            client.closeTrade(String.valueOf(tradeId));
        } catch (Exception e) {
            log.error("Error closing trade", e);
        }
    }
}