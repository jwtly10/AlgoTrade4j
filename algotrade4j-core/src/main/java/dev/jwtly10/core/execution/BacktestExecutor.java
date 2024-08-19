package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataListener;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.*;
import dev.jwtly10.core.indicators.IndicatorUtils;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.strategy.Strategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BacktestExecutor implements DataListener {
    private final Strategy strategy;
    @Getter
    private final DataManager dataManager;
    private final AccountManager accountManager;
    private final EventPublisher eventPublisher;
    private final TradeManager tradeManager;
    private final TradeStateManager tradeStateManager;
    private final PerformanceAnalyser performanceAnalyser;
    @Getter
    private final String strategyId;
    @Getter
    @Setter
    private volatile boolean initialised = false;

    public BacktestExecutor(Strategy strategy, TradeManager tradeManager, TradeStateManager tradeStateManager, AccountManager accountManager, DataManager dataManager, BarSeries barSeries, EventPublisher eventPublisher, PerformanceAnalyser performanceAnalyser) {
        this.strategyId = strategy.getStrategyId();
        this.strategy = strategy;
        this.dataManager = dataManager;
        this.eventPublisher = eventPublisher;
        this.tradeStateManager = tradeStateManager;
        this.accountManager = accountManager;
        this.tradeManager = tradeManager;
        this.performanceAnalyser = performanceAnalyser;
        strategy.onInit(barSeries, dataManager, accountManager, tradeManager, eventPublisher, performanceAnalyser);
    }

    public void initialise() {
        if (initialised) {
            log.warn("BacktestExecutor for strategy {} is already initialized", strategyId);
            return;
        }

        log.info("Initializing strategy: {}", strategyId);
        strategy.onStart();
        initialised = true;

        eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Strategy initialized"));
    }

    @Override
    public void onTick(Tick tick, Bar currentBar) {
        if (!initialised) {
            log.error("Attempt to call onTick for uninitialized BacktestExecutor for strategy: {}", strategyId);
            return;
        }
        eventPublisher.publishEvent(new BarEvent(strategyId, currentBar.getInstrument(), currentBar));
        strategy.onTick(tick, currentBar);
        tradeManager.setCurrentTick(tick);
        tradeStateManager.updateTradeStates(accountManager, tradeManager, tick);
        performanceAnalyser.updateOnTick(accountManager.getEquity(), tick.getDateTime());
    }

    @Override
    public void onBarClose(Bar closedBar) {
        if (!initialised) {
            log.error("Attempt to call onBarClose for uninitialized BacktestExecutor for strategy: {}", strategyId);
            return;
        }
        // Update indicators on bar close TODO: Some indicators may need tick data, so we may need to update them on tick as well. TBC
        IndicatorUtils.updateIndicators(strategy, closedBar);
        strategy.onBarClose(closedBar);
        log.debug("Bar: {}, Balance: {}, Equity: {}", closedBar, accountManager.getBalance(), accountManager.getEquity());
    }

    @Override
    public void onStop() {
        if (!initialised) {
            log.error("Attempt to stop uninitialized BacktestExecutor for strategy: {}", strategyId);
            return;
        }
        cleanup();
    }

    private void cleanup() {
        log.debug("Cleaning up strategy");
        tradeManager.getOpenTrades().values().forEach(trade -> {
            tradeManager.closePosition(trade.getId());
        });
        // Update trade states one last time
        tradeStateManager.updateTradeStates(accountManager, tradeManager, null);
        // Run final performance analysis
        performanceAnalyser.calculateStatistics(tradeManager.getAllTrades(), accountManager.getInitialBalance());

        // Spin down the strategy
        strategy.onDeInit();
        strategy.onEnd();

        // Publish final events
        eventPublisher.publishEvent(new StrategyStopEvent(strategyId, "Strategy stopped"));
        eventPublisher.publishEvent(new AnalysisEvent(strategyId, dataManager.getInstrument(), performanceAnalyser));
        eventPublisher.publishEvent(new AccountEvent(strategyId, accountManager.getAccount()));

        initialised = false;
    }
}