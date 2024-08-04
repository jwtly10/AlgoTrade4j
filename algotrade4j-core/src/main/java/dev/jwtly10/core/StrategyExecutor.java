package dev.jwtly10.core;

import dev.jwtly10.core.backtest.BacktestTradeManager;
import dev.jwtly10.core.datafeed.DataFeed;
import dev.jwtly10.core.datafeed.DataFeedException;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.StrategyStopEvent;
import dev.jwtly10.core.indicators.IndicatorUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class StrategyExecutor implements BarDataListener {
    private final BarSeries barSeries;
    private final Strategy strategy;
    private final List<Indicator> indicators;
    private final DataFeed dataFeed;
    private final EventPublisher eventPublisher;
    private final TradeManager tradeManager;
    private final String strategyId;
    private volatile boolean running = false;

    public StrategyExecutor(Strategy strategy, PriceFeed priceFeed, BarSeries barSeries, DataFeed dataFeed, Number initialCash, EventPublisher eventPublisher) {
        this.strategyId = strategy.getStrategyId();
        this.strategy = strategy;
        this.dataFeed = dataFeed;
        this.indicators = new ArrayList<>();
        this.barSeries = barSeries;
        this.eventPublisher = eventPublisher;
        this.tradeManager = new BacktestTradeManager(strategyId, initialCash, priceFeed, eventPublisher);
        strategy.onInit(barSeries, priceFeed, tradeManager, eventPublisher);
    }

    /**
     * Runs the strategy.
     * This will subscribe the executor to a datafeed, and then start the data feed in a virtual thread
     * In a live trading environment, the data feed would be connected to a broker.
     * The strategy will be initialized, and then the executor will wait for bar data to be received, and then
     * call the strategy's onBar method.
     *
     * @throws DataFeedException if there is an error starting the data feed
     */
    public void run() throws DataFeedException {
        running = true;
        // TODO: On init we should load all trades from broker (when in live mode)
        // We should also load a number of bars, depending on the indicator
        strategy.onStart();
        dataFeed.addBarDataListener(this);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                try {
                    dataFeed.start();
                } catch (DataFeedException e) {
                    log.error("Data feed error", e);
                    stop();
                }
            });

            while (running) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stop();
        }

        cleanup();
    }

    /*
     * This method is called when a new bar is received from the data feed.
     * The bar is added to the bar series, and then the strategy is called with the new bar.
     * The strategy will then update any indicators, and then update any trades.
     * Finally, the strategies on bar event will be called with the updated bar series, indicators, and trades.
     * @param bar the new bar received from the data feed
     */
    @Override
    public void onBar(Bar bar) {
        if (!running) {
            return;
        }

        barSeries.addBar(bar);
        eventPublisher.publishEvent(new BarEvent(strategyId, bar.getSymbol(), bar));

        IndicatorUtils.updateIndicators(strategy, bar);

        tradeManager.updateTrades(bar);
        strategy.onBar(bar, barSeries, indicators, tradeManager);

        // Print account information after each bar
        Account account = tradeManager.getAccount();
        log.info("Bar: {}, Balance: {}, Equity: {}, Open Position Value: {}", bar.getDateTime(), account.getBalance(), account.getEquity(), account.getOpenPositionValue());
    }


    public void stop() {
        running = false;
        try {
            dataFeed.stop();
            dataFeed.removeBarDataListener(this);
        } catch (DataFeedException e) {
            log.error("Error stopping data feed", e);
        }
    }

    private void cleanup() {
        log.info("Cleaning up strategy");
        strategy.onDeInit();
        eventPublisher.publishEvent(new StrategyStopEvent(
                strategyId,
                "Strategy stopped"
        ));
    }

    public void addIndicator(Indicator indicator) {
        indicators.add(indicator);
    }

    @Override
    public void onStop() {
        stop();
    }
}