package dev.jwtly10.marketdata.oanda.models;

/**
 * https://developer.oanda.com/rest-live-v20/trade-df/#TradeStateFilter
 */
public enum TradeStateFilter {
    OPEN,
    CLOSED,
    CLOSE_WHEN_TRADEABLE,
    ALL
}