package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.data.DataListener;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.StrategyStopEvent;
import dev.jwtly10.core.indicators.IndicatorUtils;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.strategy.Strategy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class StrategyExecutor implements DataListener {
    private final BarSeries barSeries;
    private final Strategy strategy;
    private final DataManager dataManager;
    private final AccountManager accountManager;
    private final EventPublisher eventPublisher;
    private final TradeManager tradeManager;
    private final TradeStateManager tradeStateManager;
    private final String strategyId;
    @Setter
    private volatile boolean running = false;

    public StrategyExecutor(Strategy strategy, TradeManager tradeManager, TradeStateManager tradeStateManager, AccountManager accountManager, DataManager dataManager, BarSeries barSeries, EventPublisher eventPublisher) {
        this.strategyId = strategy.getStrategyId();
        this.strategy = strategy;
        this.dataManager = dataManager;
        this.barSeries = barSeries;
        this.eventPublisher = eventPublisher;
        this.tradeStateManager = tradeStateManager;
        this.accountManager = accountManager;
        this.tradeManager = tradeManager;
        strategy.onInit(barSeries, dataManager, accountManager, tradeManager, eventPublisher);
    }

    public void run() throws Exception {
        running = true;
        log.info("Running strategy: {}", strategyId);
        strategy.onStart();
        dataManager.addDataListener(this);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                try {
                    dataManager.start();
                } catch (Exception e) {
                    log.error("Data manager error", e);
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
    public void onTick(Tick tick, Bar currentBar) {
        if (!running) return;
        eventPublisher.publishEvent(new BarEvent(strategyId, currentBar.getSymbol(), currentBar));
        strategy.onTick(tick, currentBar);
        tradeManager.setCurrentTick(tick);
        tradeStateManager.updateTradeStates(accountManager, tradeManager, tick);
    }

    @Override
    public void onBarClose(Bar closedBar) {
        if (!running) return;
        // Update indicators on bar close TODO: Some indicators may need tick data, so we may need to update them on tick as well. TBC
        IndicatorUtils.updateIndicators(strategy, closedBar);
        strategy.onBarClose(closedBar);
        log.debug("Bar: {}, Balance: {}, Equity: {}", closedBar, accountManager.getBalance(), accountManager.getEquity());
        barSeries.addBar(closedBar);
    }

    public void stop() {
        running = false;
        try {
            dataManager.stop();
            dataManager.removeDataListener(this);
        } catch (Exception e) {
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