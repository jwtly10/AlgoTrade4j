package dev.jwtly10.core;

import java.time.ZonedDateTime;

public class MockPriceFeed implements PriceFeed {
    private Number bid;
    private Number ask;
    private ZonedDateTime dateTime;

    public void setPrice(Number price) {
        this.bid = price.subtract(new Number("0.01"));
        this.ask = price.add(new Number("0.01"));
        this.dateTime = ZonedDateTime.now();
    }

    @Override
    public Number getBid(String symbol) {
        return bid;
    }

    @Override
    public ZonedDateTime getDateTime(String symbol) {
        return dateTime;
    }

    @Override
    public Number getAsk(String symbol) {
        return ask;
    }

    @Override
    public Number getOpen(String symbol) {
        return null;
    }

    @Override
    public Number getHigh(String symbol) {
        return null;
    }

    @Override
    public Number getLow(String symbol) {
        return null;
    }

    @Override
    public Number getClose(String symbol) {
        return null;
    }
}