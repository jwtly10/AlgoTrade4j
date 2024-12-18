package dev.jwtly10.liveapi.executor;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.data.DataListener;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.BarEvent;
import dev.jwtly10.core.event.types.LogEvent;
import dev.jwtly10.core.event.types.async.AsyncIndicatorsEvent;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.external.news.StrategyNewsUtil;
import dev.jwtly10.core.external.notifications.Notifier;
import dev.jwtly10.core.indicators.Indicator;
import dev.jwtly10.core.indicators.IndicatorUtils;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.risk.RiskManagementService;
import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.liveapi.exception.LiveExecutorException;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LiveExecutor implements DataListener {
    private final Strategy strategy;
    private final TradeManager tradeManager;
    private final AccountManager accountManager;
    private final EventPublisher eventPublisher;
    private final LiveStrategyService liveStrategyService;
    @Getter
    private final DataManager dataManager;
    private final String strategyId;
    private final LiveStateManager liveStateManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Notifier notifier;

    @Getter
    @Setter
    private volatile boolean initialised = false;

    public LiveExecutor(Strategy strategy,
                        TradeManager tradeManager,
                        AccountManager accountManager,
                        DataManager dataManager,
                        EventPublisher eventPublisher,
                        LiveStateManager liveStateManager,
                        RiskManagementService riskManagementService,
                        Notifier notifier,
                        LiveStrategyService liveStrategyService,
                        StrategyNewsUtil strategyNewsUtil
    ) {
        this.strategy = strategy;
        this.tradeManager = tradeManager;
        this.dataManager = dataManager;
        this.accountManager = accountManager;
        this.eventPublisher = eventPublisher;
        this.strategyId = strategy.getStrategyId();
        this.liveStateManager = liveStateManager;
        this.notifier = notifier;
        this.liveStrategyService = liveStrategyService;
        tradeManager.setOnTradeCloseCallback(this::onTradeClose);
        strategy.onInit(dataManager.getBarSeries(), dataManager, accountManager, tradeManager, eventPublisher, riskManagementService, null, notifier, strategyNewsUtil);
    }

    @Override
    public void initialise() throws Exception {
        if (initialised) {
            log.warn("LiveExecutor for strategy {} is already initialized", strategyId);
            return;
        }

        log.info("Initializing strategy: {} with parameters: {}", strategyId, ParameterHandler.getParameters(strategy));

        initialised = true;

        //  Load all the trades from the broker into memory
        tradeManager.start();
        tradeManager.loadTrades();
        strategy.onStart();

        if (!this.dataManager.getBarSeries().isEmpty()) {
            IndicatorUtils.initializeIndicators(strategy, this.dataManager.getBarSeries().getBars());
            getIndicators();
            Map<String, List<IndicatorValue>> allIndicatorsValues = getIndicators();
            eventPublisher.publishEvent(new AsyncIndicatorsEvent(strategyId, dataManager.getInstrument(), allIndicatorsValues));
        }

        // Start polling for account/trade data
        log.info("Starting live state manager for strategy: {}", strategyId);
        scheduler.scheduleAtFixedRate(liveStateManager::updateState, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onTick(Tick tick, Bar currentBar) {
        if (!initialised) {
            log.error("Attempt to call onTick for uninitialized LiveExecutor for strategy: {}", strategyId);
            return;
        }

        try {
            tradeManager.setCurrentTick(tick);
            eventPublisher.publishEvent(new BarEvent(strategyId, currentBar.getInstrument(), currentBar));

            strategy.onTick(tick, currentBar);
        } catch (Exception e) {
            log.error("Error processing tick data: {}", e.getMessage(), e);
            throw new LiveExecutorException(strategyId, "Error processing tick data", e);
        }
    }

    @Override
    public void onBarClose(Bar closedBar) {
        if (!initialised) {
            log.error("Attempt to call onBarClose for uninitialized LiveExecutor for strategy: {}", strategyId);
            return;
        }
        try {
            IndicatorUtils.updateIndicators(strategy, closedBar);
            strategy.onBarClose(closedBar);
            log.trace("Bar: {}, Balance: {}, Equity: {}", closedBar, accountManager.getBalance(), accountManager.getEquity());
        } catch (Exception e) {
            log.error("Error processing bar close for strategy: {}", strategyId, e);
            throw new LiveExecutorException(strategyId, "Error processing bar close", e);
        }
    }

    @Override
    public void onNewDay(ZonedDateTime newDay) {
        if (!initialised) {
            log.error("Attempt to call onNewDay for uninitialized LiveExecutor for strategy: {}", strategyId);
            return;
        }
        try {
            strategy.onNewDay(newDay);
        } catch (Exception e) {
            log.error("Error processing new day for strategy: {}", strategyId, e);
            throw new LiveExecutorException(strategyId, "Error processing new day", e);
        }
    }

    @Override
    public void onStop(String reason) {
        if (!initialised) {
            log.error("Attempt to stop uninitialized LiveExecutor for strategy: {}", strategyId);
            return;
        }
        cleanup(reason);
    }

    @Override
    public void onTradeClose(Trade trade) {
        log.info("(Callback) Trade closed @ {} : id={}, profit={}, closePrice={}, stopLoss={}, takeProfit={}", trade.getCloseTime(), trade.getId(), trade.getProfit(), trade.getClosePrice(), trade.getStopLoss(), trade.getTakeProfit());
        eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Trade [%s] closed for strategy '%s' with profit '%s'", trade.getId(), strategyId, trade.getProfit()));
        strategy.onTradeClose(trade);
    }

    private void cleanup(String reason) {
        log.info("Strategy {} stopped with reason: '{}'.", strategyId, reason);
        notifier.sendSysNotification(String.format("Live Strategy '%s' stopped with reason: '%s'", strategyId, reason), true);
        scheduler.shutdown();

        // Kill the live state manager
        scheduler.shutdownNow();
        // Shutdown any processes in the trade manager
        tradeManager.shutdown();

        try {
            log.info("Deactivating strategy: {}", strategyId);
            liveStrategyService.deactivateStrategy(strategyId);
        } catch (Exception e) {
            log.warn("Error deactivating strategy: {}", strategyId, e);
        }

        strategy.onDeInit();
        strategy.onEnd();

        initialised = false;

        MDC.clear();
    }

    @Override
    public String getStrategyId() {
        return this.strategyId;
    }

    public void closeTrade(String tradeId) throws RuntimeException {
        try {
            tradeManager.closePosition(Integer.parseInt(tradeId), true);
        } catch (Exception e) {
            log.error("Error closing trade: {}", tradeId, e);
            throw new LiveExecutorException(strategyId, e.getMessage(), e);
        }
    }

    public List<Bar> getBars() {
        return dataManager.getBarSeries().getBars();
    }

    public BarSeries getBarSeries() {
        return dataManager.getBarSeries();
    }

    public Instrument getInstrument() {
        return dataManager.getInstrument();
    }

    public Map<Integer, Trade> getTrades() {
        return tradeManager.getAllTrades();
    }

    public Map<String, List<IndicatorValue>> getIndicators() {
        Map<String, List<IndicatorValue>> allIndicatorsValues = new HashMap<>();
        for (Indicator i : strategy.getIndicators()) {
            allIndicatorsValues.put(i.getName(), i.getValues());
        }

        return allIndicatorsValues;
    }
}