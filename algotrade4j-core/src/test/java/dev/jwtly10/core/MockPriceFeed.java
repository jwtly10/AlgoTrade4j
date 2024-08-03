package dev.jwtly10.core;

public class MockPriceFeed implements PriceFeed {
    private Number bid;
    private Number ask;

    public void setPrice(Number price) {
        this.bid = price.subtract(new Number("0.01"));
        this.ask = price.add(new Number("0.01"));
    }

    @Override
    public Number getBid(String symbol) {
        return bid;
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