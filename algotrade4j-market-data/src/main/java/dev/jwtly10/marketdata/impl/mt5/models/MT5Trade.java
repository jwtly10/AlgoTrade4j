package dev.jwtly10.marketdata.impl.mt5.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jwtly10.core.model.Broker;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// TODO: we should support passing the timezone for more accurate time conversions....
public record MT5Trade(
        @JsonProperty("position_id") Integer positionId,
        @JsonProperty("symbol") String symbol,
        @JsonProperty("total_volume") Double totalVolume,
        @JsonProperty("is_long") Boolean isLong,
        @JsonProperty("open_order_ticket") Integer openOrderTicket,
        @JsonProperty("open_order_price") Double openOrderPrice,
        @JsonProperty("open_order_time") Integer openOrderTime,
        @JsonProperty("stop_loss") Double stopLoss,
        @JsonProperty("take_profit") Double takeProfit,
        @JsonProperty("profit") Double profit,
        @JsonProperty("close_order_ticket") Integer closeOrderTicket,
        @JsonProperty("close_order_price") Double closeOrderPrice,
        @JsonProperty("close_order_time") Integer closeOrderTime,
        @JsonProperty("is_open") Boolean isOpen
) {

    public Trade toTrade(Broker broker, ZoneId brokerZoneId) {
        Trade trade = new Trade(
                positionId,
                Instrument.fromBrokerSymbol(broker, symbol),
                Math.abs(totalVolume),
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(openOrderTime), brokerZoneId),
                new Number(openOrderPrice),
                new Number(stopLoss),
                new Number(takeProfit),
                isLong);

        trade.setProfit(profit);

        if (!isOpen) {
            trade.setClosePrice(new Number(closeOrderPrice));
            trade.setCloseTime(ZonedDateTime.ofInstant(Instant.ofEpochSecond(closeOrderTime), ZoneId.systemDefault()));
        }

        return trade;
    }
}