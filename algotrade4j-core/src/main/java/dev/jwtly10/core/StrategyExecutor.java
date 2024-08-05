package dev.jwtly10.core;

import dev.jwtly10.core.datafeed.DataFeed;
import dev.jwtly10.core.datafeed.DataFeedException;
import dev.jwtly10.core.defaults.DefaultBar;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.StrategyStopEvent;
import dev.jwtly10.core.indicators.IndicatorUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class StrategyExecutor implements MarketDataListener {
    private final BarSeries barSeries;
    private final Strategy strategy;
    private final DataFeed dataFeed;
    private final EventPublisher eventPublisher;
    private final TradeManager tradeManager;
    private final String strategyId;
    private Bar currentBar;
    @Setter
    private volatile boolean running = false;

    public StrategyExecutor(Strategy strategy, TradeManager tradeManager, PriceFeed priceFeed, BarSeries barSeries, DataFeed dataFeed, EventPublisher eventPublisher) {
        this.strategyId = strategy.getStrategyId();
        this.strategy = strategy;
        this.dataFeed = dataFeed;
        this.barSeries = barSeries;
        this.eventPublisher = eventPublisher;
        this.tradeManager = tradeManager;
        strategy.onInit(barSeries, priceFeed, tradeManager, eventPublisher);
    }

    public void run() throws DataFeedException {
        running = true;
        log.info("Running strategy: {}", strategyId);
        strategy.onStart();
        dataFeed.addMarketDataListener(this);

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

    @Override
    public void onTick(Tick tick) {
        if (!running) return;

        if (this.currentBar == null ||
                (tick.getDateTime().isAfter(this.currentBar.getOpenTime()) &&
                        tick.getDateTime().isBefore(this.currentBar.getCloseTime()))) {
            // If the current bar is not available (first tick) or the tick is outside the current bar
            if (this.currentBar != null) {
                // If tick is outside the current bar, add it to the series, and notify the strategy of the bar closing
                barSeries.addBar(this.currentBar);
                onBarClose(this.currentBar);
            }
            // If the current bar is not available, or the tick is outside the current bar, create a new bar
            // This only happens on the first tick, or when the tick is outside the current bar
            this.currentBar = new DefaultBar(tick.getSymbol(), Duration.ofDays(1), tick.getDateTime(), tick.getPrice(), tick.getPrice(), tick.getPrice(), tick.getPrice(), tick.getVolume());
        } else {
            this.currentBar.update(tick);
            // TODO: Check how this would work - we should keep updating the last value of the indicator basic on this partial bar.
            IndicatorUtils.updateIndicators(strategy, this.currentBar);
        }

        // Now we can send the tick, and the current bar to the strategy
        strategy.onTick(tick, this.currentBar);
        // Update trades on tick
        tradeManager.updateTrades(tick);
    }

    @Override
    public void onBarClose(Bar closedBar) {
        if (!running) return;

        eventPublisher.publishEvent(new BarEvent(strategyId, closedBar.getSymbol(), closedBar));
        // Update indicators on bar close TODO: Some indicators may need tick data, so we may need to update them on tick as well. TBC
        IndicatorUtils.updateIndicators(strategy, closedBar);

        strategy.onBarClose(closedBar);

        Account account = tradeManager.getAccount();
        log.debug("Bar: {}, Balance: {}, Equity: {}, Open Position Value: {}", closedBar, account.getBalance(), account.getEquity(), account.getOpenPositionValue());
    }

    public void stop() {
        running = false;
        try {
            dataFeed.stop();
            dataFeed.removeMarketDataListener(this);
        } catch (DataFeedException e) {
            log.error("Error stopping data feed", e);
        }
    }

    private void cleanup() {
        log.info("Cleaning up strategy");
        strategy.onDeInit();
        eventPublisher.publishEvent(new StrategyStopEvent(strategyId, "Strategy stopped"));
    }

    @Override
    public void onStop() {
        stop();
    }
}