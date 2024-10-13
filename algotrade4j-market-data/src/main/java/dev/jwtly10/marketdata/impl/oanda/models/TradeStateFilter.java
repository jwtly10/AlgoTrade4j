package dev.jwtly10.marketdata.impl.oanda.models;

/**
 * https://developer.oanda.com/rest-live-v20/trade-df/#TradeStateFilter
 */
public enum TradeStateFilter {
    OPEN,
    CLOSED,
    CLOSE_WHEN_TRADEABLE,
    ALL
}