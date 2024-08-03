package dev.jwtly10.core;

import dev.jwtly10.core.backtest.BacktestPriceFeed;
import dev.jwtly10.core.backtest.BacktestTradeManager;
import dev.jwtly10.core.datafeed.DataFeed;
import dev.jwtly10.core.datafeed.DataFeedException;
import dev.jwtly10.core.defaults.DefaultBarSeries;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.EventPublisher;

import java.util.ArrayList;
import java.util.List;

public class StrategyExecutor implements BarDataListener {
    private final BarSeries barSeries;
    private final Strategy strategy;
    private final List<Indicator> indicators;
    private final DataFeed dataFeed;
    private final EventPublisher eventPublisher;
    private final TradeManager tradeManager;

    private final String strategyId;

    public StrategyExecutor(Strategy strategy, DataFeed dataFeed, Number initialCash, int barSeriesSize, EventPublisher eventPublisher) {
        this.strategyId = strategy.getStrategyId();
        this.strategy = strategy;
        this.dataFeed = dataFeed;
        this.indicators = new ArrayList<>();
        this.barSeries = new DefaultBarSeries(barSeriesSize);
        PriceFeed priceFeed = new BacktestPriceFeed(this.barSeries, new Number(10));
        this.eventPublisher = eventPublisher;
        this.tradeManager = new BacktestTradeManager(strategyId, initialCash, priceFeed, eventPublisher);
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
        eventPublisher.publishEvent(new BarEvent(strategyId, bar.getSymbol(), bar));

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