package dev.jwtly10.core;

import dev.jwtly10.core.backtest.BacktestPriceFeed;
import dev.jwtly10.core.backtest.BacktestTradeManager;
import dev.jwtly10.core.datafeed.DataFeed;
import dev.jwtly10.core.datafeed.DataFeedException;

import java.util.ArrayList;
import java.util.List;

public class StrategyExecutor implements BarDataListener {
    private final BarSeries barSeries;
    private final Strategy strategy;
    private final List<Indicator> indicators;
    private final DataFeed dataFeed;
    private final TradeManager tradeManager;
    private final PriceFeed priceFeed;

    public StrategyExecutor(Strategy strategy, DataFeed dataFeed, Number initialCash, int barSeriesSize) {
        this.strategy = strategy;
        this.dataFeed = dataFeed;
        this.indicators = new ArrayList<>();
        this.barSeries = new DefaultBarSeries(barSeriesSize);
        this.priceFeed = new BacktestPriceFeed(this.barSeries, new Number(10));
        this.tradeManager = new BacktestTradeManager(initialCash, priceFeed);
    }

    public void run() throws DataFeedException {
        // TODO: On init we should load all trades from broker (when in live mode)
        strategy.onInit(barSeries, indicators, tradeManager);
        dataFeed.addBarDataListener(this);
        dataFeed.start();
    }

    public void addIndicator(Indicator indicator) {
        indicators.add(indicator);
    }

    @Override
    public void onBar(Bar bar) {
        barSeries.addBar(bar);

        for (Indicator indicator : indicators) {
            indicator.update(bar);
        }

        tradeManager.updateTrades(bar);
        strategy.onBar(bar, barSeries, indicators, tradeManager);

        // Print account information after each bar
        Account account = tradeManager.getAccount();
        System.out.println("Bar: " + bar.getDateTime() +
                ", Balance: " + account.getBalance() +
                ", Equity: " + account.getEquity() +
                ", Open Position Value: " + account.getOpenPositionValue());
    }
}