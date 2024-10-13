package dev.jwtly10.marketdata.impl.oanda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.marketdata.impl.oanda.request.MarketOrderRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * https://developer.oanda.com/rest-live-v20/trade-df/#Trade
 */
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaTrade(
        String id,
        String instrument,
        String openTime,
        String price,
        double initialUnits,
        String state,
        double currentUnits,
        double realizedPL,
        double unrealizedPL,
        List<String> closingTransactionIDs,
        String closeTime,
        double averageClosePrice,
        String lastTransactionID,
        MarketOrderRequest.TakeProfitDetails takeProfitOrder,
        MarketOrderRequest.StopLossDetails stopLossOrder
) {
    /**
     * Converts this OandaTrade to a Internal system Trade.
     * Note:
     * Oanda quantity is NEGATIVE for short trades and POSITIVE for long trades.
     *
     * @return the trade
     */
    public Trade toTrade() {
        // These should be there for every trade
        Trade trade = new Trade(
                Integer.parseInt(id), // external id
                Instrument.fromOandaSymbol(instrument), // instrument
                Math.abs(initialUnits), // Quantity
                ZonedDateTime.parse(openTime), // open time
                new Number(price), // entry price
                new Number(stopLossOrder != null ? stopLossOrder.price() : "0"), // stop loss
                new Number(takeProfitOrder != null ? takeProfitOrder.price() : "0"), // take profit
                initialUnits > 0 // is long ?
        );

        // These are optional, depending on the trade type
        if (state.equals("OPEN")) {
            trade.setProfit(unrealizedPL);
        } else if (state.equals("CLOSED")) {
            trade.setCloseTime(ZonedDateTime.parse(closeTime));
            trade.setClosePrice(new Number(averageClosePrice));
            trade.setProfit(realizedPL);
        } else {
            log.warn("Unknown trade state: {}", state);
        }

        return trade;
    }
}