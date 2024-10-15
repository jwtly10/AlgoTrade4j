package dev.jwtly10.marketdata.impl.oanda.response.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderFillTransaction(
        String id,
        String accountID,
        long userID,
        String requestID,
        String time,
        String type,
        String orderID,
        String instrument,
        String units,
        String price,
        String pl,
        String accountBalance,
        String reason,
        List<TradesClosed> tradesClosed
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TradesClosed(
            String tradeID,
            String clientTradeID,
            String units,
            String realizedPL,
            String financing,
            String price
    ) {
    }
}