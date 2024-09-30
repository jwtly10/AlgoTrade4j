package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataListener;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.*;
import dev.jwtly10.core.event.async.*;
import dev.jwtly10.core.exception.BacktestExecutorException;
import dev.jwtly10.core.indicators.Indicator;
import dev.jwtly10.core.indicators.IndicatorUtils;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.risk.RiskManager;
import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BacktestExecutor implements DataListener {
    private final Strategy strategy;
    @Getter
    private final DataManager dataManager;
    private final AccountManager accountManager;
    private final EventPublisher eventPublisher;
    private final TradeManager tradeManager;
    private final TradeStateManager tradeStateManager;
    private final RiskManager riskManager;
    private final PerformanceAnalyser performanceAnalyser;
    @Getter
    private final String strategyId;
    @Getter
    @Setter
    private volatile boolean initialised = false;

    public BacktestExecutor(Strategy strategy, TradeManager tradeManager, TradeStateManager tradeStateManager, AccountManager accountManager, DataManager dataManager, BarSeries barSeries, EventPublisher eventPublisher, PerformanceAnalyser performanceAnalyser, RiskManager riskManager) {
        this.strategyId = strategy.getStrategyId();
        this.strategy = strategy;
        this.dataManager = dataManager;
        this.eventPublisher = eventPublisher;
        this.tradeStateManager = tradeStateManager;
        this.accountManager = accountManager;
        this.tradeManager = tradeManager;
        this.performanceAnalyser = performanceAnalyser;
        this.riskManager = riskManager;
        tradeManager.setOnTradeCloseCallback(this::onTradeClose);
        strategy.onInit(barSeries, dataManager, accountManager, tradeManager, eventPublisher, performanceAnalyser);
    }

    @Override
    public void initialise() {
        if (initialised) {
            log.warn("BacktestExecutor for strategy {} is already initialized", strategyId);
            return;
        }

        log.info("Initializing strategy: {} with parameters: {}", strategyId, ParameterHandler.getParameters(strategy));

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
        try {
            tradeManager.setCurrentTick(tick);
            eventPublisher.publishEvent(new BarEvent(strategyId, currentBar.getInstrument(), currentBar));
            tradeStateManager.updateTradeProfitStateOnTick(tradeManager, tick);
            tradeStateManager.updateAccountEquityOnTick(accountManager, tradeManager);
            performanceAnalyser.updateOnTick(accountManager.getEquity());

            riskManager.check(tick, tradeManager);

            // All analysis should be done before calling the strategy

            strategy.onTick(tick, currentBar);
        } catch (Exception e) {
            throw new BacktestExecutorException(strategyId, "Strategy failed due to: ", e);
        }
    }

    @Override
    public void onBarClose(Bar closedBar) {
        if (!initialised) {
            log.error("Attempt to call onBarClose for uninitialized BacktestExecutor for strategy: {}", strategyId);
            return;
        }
        try {
            // Update indicators on bar close TODO: Some indicators may need tick data, so we may need to update them on tick as well. TBC
            IndicatorUtils.updateIndicators(strategy, closedBar);
            strategy.onBarClose(closedBar);
            log.trace("Bar: {}, Balance: {}, Equity: {}", closedBar, accountManager.getBalance(), accountManager.getEquity());
            performanceAnalyser.updateOnBar(accountManager.getEquity(), closedBar.getCloseTime());
        } catch (Exception e) {
            throw new BacktestExecutorException(strategyId, "Strategy failed due to: ", e);
        }
    }

    @Override
    public void onNewDay(ZonedDateTime newDay) {
        if (!initialised) {
            log.error("Attempt to call onNewDay for uninitialized BacktestExecutor for strategy: {}", strategyId);
            return;
        }
        try {
            strategy.onNewDay(newDay);
            // Here we can trigger an async event to notify the async callers that a new day has passed. This will also let us notify frontend of progress each day
            eventPublisher.publishEvent(new AsyncProgressEvent(strategyId, dataManager.getInstrument(), dataManager.getFrom(), dataManager.getTo(), newDay, dataManager.getTicksModeled()));
        } catch (Exception e) {
            throw new BacktestExecutorException(strategyId, "Strategy failed due to: ", e);
        }
    }

    @Override
    public void onTradeClose(Trade trade) {
        tradeStateManager.updateBalanceOnTradeClose(trade, accountManager);
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
        log.info("Cleaning up strategy and closing any open trades");
        tradeManager.getOpenTrades().values().forEach(trade -> {
            tradeManager.closePosition(trade.getId(), false);
        });
        // Update trade states and account state
        tradeStateManager.updateTradeProfitStateOnTick(tradeManager, null);
        tradeStateManager.updateAccountEquityOnTick(accountManager, tradeManager);

        // Run final performance analysis
        performanceAnalyser.calculateStatistics(tradeManager.getAllTrades(), accountManager.getInitialBalance());

        // Shutdown any processes in the trade manager
        tradeManager.shutdown();

        // Spin down the strategy
        strategy.onDeInit();
        strategy.onEnd();

        // Publish final events
        eventPublisher.publishEvent(new AnalysisEvent(strategyId, dataManager.getInstrument(), performanceAnalyser));
        eventPublisher.publishEvent(new AccountEvent(strategyId, accountManager.getAccount()));

        // Async specific events
        eventPublisher.publishEvent(new AsyncAccountEvent(strategyId, accountManager.getAccount()));
        eventPublisher.publishEvent(new AsyncBarSeriesEvent(strategyId, dataManager.getInstrument(), dataManager.getBarSeries()));
        eventPublisher.publishEvent(new AsyncTradesEvent(strategyId, dataManager.getInstrument(), tradeManager.getAllTrades()));
        // Generate structure for all indicator data
        Map<String, List<IndicatorValue>> allIndicatorsValues = new HashMap<>();
        for (Indicator i : strategy.getIndicators()) {
            allIndicatorsValues.put(i.getName(), i.getValues());
        }
        eventPublisher.publishEvent(new AsyncIndicatorsEvent(strategyId, dataManager.getInstrument(), allIndicatorsValues));

        // This should always happen last, as there may be some logic a client needs to handle once all events are complete
        eventPublisher.publishEvent(new StrategyStopEvent(strategyId, "Strategy stopped"));
        initialised = false;
    }

}