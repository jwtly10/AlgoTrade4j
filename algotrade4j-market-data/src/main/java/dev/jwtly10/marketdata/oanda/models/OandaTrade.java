package dev.jwtly10.marketdata.oanda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.marketdata.oanda.request.MarketOrderRequest;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * https://developer.oanda.com/rest-live-v20/trade-df/#Trade
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaTrade(
        String id,
        String instrument,
        String openTime,
        String price,
        double initialUnits,
        String state,
        double currentUnits,
        double realisedPL,
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
        return new Trade(
                Integer.parseInt(id), // external id
                Instrument.fromOandaSymbol(instrument), // instrument
                Math.abs(initialUnits), // Quantity
                ZonedDateTime.parse(openTime), // open time
                new Number(price), // entry price
                new Number(stopLossOrder.price()), // stop loss
                new Number(takeProfitOrder.price()), // take profit
                initialUnits > 0 // is long ?
        );
    }
}