package dev.jwtly10.marketdata.oanda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jwtly10.marketdata.oanda.request.MarketOrderRequest;

/**
 * OandaOrder
 * https://developer.oanda.com/rest-live-v20/order-df/#Order
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OandaOrder {
    @JsonProperty("order")
    private final MarketOrderRequest order;

    public OandaOrder(MarketOrderRequest order) {
        this.order = order;
    }
}