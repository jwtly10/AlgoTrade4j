package dev.jwtly10.marketdata.impl.oanda.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.marketdata.impl.oanda.request.MarketOrderRequest;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaOpenTradeResponse(
        OrderCreateTransaction orderCreateTransaction,
        OrderFillTransaction orderFillTransaction,
        List<String> relatedTransactionIDs,
        String lastTransactionId
) {

    public enum Type {
        MARKET_ORDER,
        ORDER_FILL
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderCreateTransaction(
            String id,
            String accountID,
            String userID,
            String requestID,
            String time,
            Type type,
            String instrument,
            double units,
            MarketOrderRequest.TakeProfitDetails takeProfitOnFill,
            MarketOrderRequest.StopLossDetails stopLossOnFill,
            String reason
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderFillTransaction(
            String id,
            String accountID,
            String userID,
            String requestID,
            String time,
            String orderID,
            String price,
            Type type,
            double units,
            double requestedUnits,
            TradeOpened tradeOpened
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TradeOpened(
            String price,
            String tradeID,
            double units,
            String initialMarginRequired
    ) {

    }

}