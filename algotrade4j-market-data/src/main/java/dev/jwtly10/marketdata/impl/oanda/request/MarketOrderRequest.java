package dev.jwtly10.marketdata.impl.oanda.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record MarketOrderRequest(
        OrderType type,
        String instrument,
        double units,
        TimeInForce timeInForce,
        OrderPositionFill orderPositionFill,
        TakeProfitDetails takeProfitOnFill,
        StopLossDetails stopLossOnFill
//        TrailingStopLossDetails trailingStopLossOnFill // May implement this in future
) {

    @Builder
    public MarketOrderRequest {
    }

    /**
     * <a href="https://developer.oanda.com/rest-live-v20/order-df/#OrderType">...</a>
     */
    public enum OrderType {
        MARKET,
        LIMIT,
        STOP,
        TAKE_PROFIT,
        STOP_LOSS
    }

    /**
     * <a href="https://developer.oanda.com/rest-live-v20/order-df/#TimeInForce">...</a>
     */
    public enum TimeInForce {
        GTC, // Good until cancelled
        GTD,
        GFD,
        FOK, // Filled or killed
        IOC;
//
//        @JsonValue
//        @Override
//        public String toString() {
//            return this.name();
//        }
    }

    /**
     * <a href="https://developer.oanda.com/rest-live-v20/order-df/#OrderPositionFill">...</a>
     */
    public enum OrderPositionFill {
        OPEN_ONLY,
        REDUCE_FIRST,
        REDUCE_ONLY,
        DEFAULT
    }

    public static class MarketOrderRequestBuilder {
        private OrderType type = OrderType.MARKET;
        private TimeInForce timeInForce = TimeInForce.GTC;
        private OrderPositionFill orderPositionFill = OrderPositionFill.DEFAULT;
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TakeProfitDetails(String price, TimeInForce timeInForce) {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StopLossDetails(String price, TimeInForce timeInForce) {
    }

}