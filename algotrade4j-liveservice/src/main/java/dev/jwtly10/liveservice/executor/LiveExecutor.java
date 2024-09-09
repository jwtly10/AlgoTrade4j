package dev.jwtly10.liveservice.executor;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.data.DataListener;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.LogEvent;
import dev.jwtly10.core.event.StrategyStopEvent;
import dev.jwtly10.core.exception.BacktestExecutorException;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.indicators.IndicatorUtils;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.liveservice.exception.LiveExecutorException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LiveExecutor implements DataListener {
    private final Strategy strategy;
    private final TradeManager tradeManager;
    private final AccountManager accountManager;
    private final EventPublisher eventPublisher;
    private final String strategyId;
    private final LiveStateManager liveStateManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Getter
    @Setter
    private volatile boolean initialised = false;

    public LiveExecutor(Strategy strategy, TradeManager tradeManager, AccountManager accountManager,
                        EventPublisher eventPublisher,
                        LiveStateManager liveStateManager) {
        this.strategy = strategy;
        this.tradeManager = tradeManager;
        this.accountManager = accountManager;
        this.eventPublisher = eventPublisher;
        this.strategyId = strategy.getStrategyId();
        this.liveStateManager = liveStateManager;
    }

    @Override
    public void initialise() {
        if (initialised) {
            log.warn("LiveExecutor for strategy {} is already initialized", strategyId);
            return;
        }

        log.info("Initializing strategy: {} with parameters: {}", strategyId, ParameterHandler.getParameters(strategy));

        strategy.onStart();
        initialised = true;

        // Start polling for account/trade data
        scheduler.scheduleAtFixedRate(liveStateManager::updateState, 0, 5, TimeUnit.SECONDS);
        eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Live Strategy initialized"));
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
            log.error("Error processing tick data", e);
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
            throw new BacktestExecutorException(strategyId, "Strategy failed due to: ", e);
        }
    }

    @Override
    public void onStop() {
        if (!initialised) {
            log.error("Attempt to stop uninitialized LiveExecutor for strategy: {}", strategyId);
            return;
        }
        cleanup();
    }

    private void cleanup() {
        log.debug("Cleaning up strategy");
        scheduler.shutdown();

        strategy.onDeInit();
        strategy.onEnd();

        eventPublisher.publishEvent(new StrategyStopEvent(strategyId, "Live strategy stopped"));
        initialised = false;
    }

    @Override
    public String getStrategyId() {
        return this.strategyId;
    }
}