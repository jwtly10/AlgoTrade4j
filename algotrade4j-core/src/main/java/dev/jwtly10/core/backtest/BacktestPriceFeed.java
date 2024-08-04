package dev.jwtly10.core.backtest;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.BarSeries;
import dev.jwtly10.core.Number;
import dev.jwtly10.core.PriceFeed;

import java.time.ZonedDateTime;

public class BacktestPriceFeed implements PriceFeed {
    private final BarSeries barSeries;
    private final Number spread; // Spread in price units, not percentage

    public BacktestPriceFeed(BarSeries barSeries, Number spread) {
        this.barSeries = barSeries;
        this.spread = spread;
    }

    @Override
    public Number getBid(String symbol) {
        Bar lastBar = barSeries.getLastBar();
        return (lastBar != null) ? lastBar.getClose().subtract(spread.divide(2)) : null;
    }

    @Override
    public Number getAsk(String symbol) {
        Bar lastBar = barSeries.getLastBar();
        return (lastBar != null) ? lastBar.getClose().add(spread.divide(2)) : null;
    }

    @Override
    public Number getOpen(String symbol) {
        Bar lastBar = barSeries.getLastBar();
        return (lastBar != null) ? lastBar.getOpen() : null;
    }

    @Override
    public ZonedDateTime getDateTime(String symbol) {
        Bar lastBar = barSeries.getLastBar();
        return (lastBar != null) ? lastBar.getDateTime() : null;
    }

    @Override
    public Number getHigh(String symbol) {
        Bar lastBar = barSeries.getLastBar();
        return (lastBar != null) ? lastBar.getHigh() : null;
    }

    @Override
    public Number getLow(String symbol) {
        Bar lastBar = barSeries.getLastBar();
        return (lastBar != null) ? lastBar.getLow() : null;
    }

    @Override
    public Number getClose(String symbol) {
        Bar lastBar = barSeries.getLastBar();
        return (lastBar != null) ? lastBar.getClose() : null;
    }
}