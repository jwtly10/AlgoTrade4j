package dev.jwtly10.api.service;

import dev.jwtly10.api.exception.ErrorType;
import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.data.DataProvider;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.*;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.BaseStrategy;
import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.utils.StrategyReflectionUtils;
import dev.jwtly10.marketdata.common.ExternalDataClient;
import dev.jwtly10.marketdata.common.ExternalDataProvider;
import dev.jwtly10.marketdata.dataclients.OandaDataClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StrategyManager {
    private final EventPublisher eventPublisher;
    private final ConcurrentHashMap<String, BacktestExecutor> runningStrategies = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Value("${oanda.api.key}")
    private String oandaApiKey;
    @Value("${oanda.account.id}")
    private String oandaAccountId;
    @Value("${oanda.api.url}")
    private String oandaApiUrl;

    public StrategyManager(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void startStrategy(StrategyConfig config, String strategyId) {
        config.validate();

        Duration period = config.getPeriod().getDuration();
        Number spread = config.getSpread();
        Instrument instrument = config.getInstrumentData().getInstrument();

        OandaClient oandaClient = new OandaClient(oandaApiUrl, oandaApiKey, oandaAccountId);
        ExternalDataClient externalDataClient = new OandaDataClient(oandaClient);

        // Ensure utc
        ZoneId utcZone = ZoneId.of("UTC");
        ZonedDateTime from = config.getTimeframe().getFrom().atZone(utcZone).withZoneSameInstant(utcZone);
        ZonedDateTime to = config.getTimeframe().getTo().atZone(utcZone).withZoneSameInstant(utcZone);
        // We seed the tick generation while backtesting so results can be consistent
        // TODO: implement a way to allow randomized tick generation (some frontend flag)
        DataProvider dataProvider = new ExternalDataProvider(externalDataClient, instrument, spread, period, from, to, 12345L);

        DataSpeed dataSpeed = config.getSpeed();

        dataProvider.setDataSpeed(dataSpeed);

        // TODO: How long does this actually equate too?
        int defaultSeriesSize = 4000;
        BarSeries barSeries = new DefaultBarSeries(defaultSeriesSize);

        DefaultDataManager dataManager = new DefaultDataManager(strategyId, instrument, dataProvider, period, barSeries, eventPublisher);

        Tick currentTick = new DefaultTick();
        Strategy strategy = null;
        try {
            strategy = StrategyReflectionUtils.getStrategyFromClassName(config.getStrategyClass(), strategyId);
        } catch (Exception e) {
            throw new StrategyManagerException("Error getting strategy from " + config.getStrategyClass() + ": " + e.getClass() + " " + e.getMessage(), ErrorType.INTERNAL_ERROR);
        }

        Map<String, String> runParams = config.getRunParams().stream()
                .collect(Collectors.toMap(StrategyConfig.RunParameter::getName, StrategyConfig.RunParameter::getValue));

        try {
            strategy.setParameters(runParams);
        } catch (IllegalAccessException e) {
            log.error("Error setting parameters for strategy: {}", strategyId, e);
            throw new StrategyManagerException("Error setting parameters for strategy: " + strategyId, ErrorType.INTERNAL_ERROR);
        }

        TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, strategy.getStrategyId(), eventPublisher);
        AccountManager accountManager = new DefaultAccountManager(config.getInitialCash(), config.getInitialCash(), config.getInitialCash());
        TradeStateManager tradeStateManager = new DefaultTradeStateManager(strategy.getStrategyId(), eventPublisher);
        PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();

        BacktestExecutor executor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);
        executor.initialise();
        dataManager.addDataListener(executor);

        Strategy finalStrategy = strategy;
        executorService.submit(() -> {
            Thread.currentThread().setName("StrategyExecutor-" + finalStrategy.getStrategyId());
            try {
                dataManager.start();
            } catch (Exception e) {
                log.error("Error running strategy", e);
                runningStrategies.remove(finalStrategy.getStrategyId());
                eventPublisher.publishErrorEvent(finalStrategy.getStrategyId(), e);
            }
        });

        runningStrategies.put(strategy.getStrategyId(), executor);
    }

    /**
     * Stop a running strategy.
     * This is deprecated for now, as its easier to just drop websocket connects to a strategy to trigger close
     * this also handles cases of client disconnects.
     * TODO: Remove this once sure no longer needed
     *
     * @param strategyId The ID of the strategy to stop.
     * @return True if the strategy was stopped successfully, false otherwise.
     */
    @Deprecated
    public boolean stopStrategy(String strategyId) {
        BacktestExecutor executor = runningStrategies.get(strategyId);
        if (executor == null) {
            log.warn("No executor found for strategy: {}", strategyId);
            return false;
        }

        DataManager dataManager = executor.getDataManager();
        if (dataManager != null) {
            dataManager.stop();
            runningStrategies.remove(strategyId);
            return true;
        } else {
            log.error("Data manager not found for strategy: {}", strategyId);
            return false;
        }
    }

    /**
     * Get the parameters for a given strategy class.
     *
     * @param strategyClass The class name of the strategy.
     * @return A list of parameter information.
     */
    public List<ParameterHandler.ParameterInfo> getParameters(String strategyClass) {
        Strategy strategy = null;
        try {
            strategy = StrategyReflectionUtils.getStrategyFromClassName(strategyClass, null);
        } catch (Exception e) {
            throw new StrategyManagerException("Error getting strategy from " + strategyClass + ": " + e.getClass() + " " + e.getMessage(), ErrorType.INTERNAL_ERROR);
        }

        try {
            ParameterHandler.initialize(strategy);
        } catch (IllegalAccessException e) {
            log.error("Error initializing parameters for strategy: {}", strategyClass, e);
            throw new StrategyManagerException("Error initializing parameters for strategy: " + strategyClass, ErrorType.INTERNAL_ERROR);
        }
        return ParameterHandler.getParameters(strategy);
    }

    /**
     * Get the strategies available in the system.
     * The system looks for strategies in dev.jwtly10.core
     * TODO: Make this extendable to other packages, via env vars or config.
     *
     * @return A set of strategy class names.
     */
    public Set<String> getStrategiesInSystem() {
        Reflections reflections = new Reflections("dev.jwtly10.core");
        Set<Class<? extends BaseStrategy>> strategies =
                reflections.getSubTypesOf(BaseStrategy.class);

        return strategies.stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toSet());
    }

    /**
     * Generate a unique strategy ID.
     * Being unique is not necessarily being enforced here, but if multiple strategies are generated at the same time,
     * say by different clients, this way we can ensure that different clients get data specific to their strategy run.
     *
     * @param strategyClass The class name of the strategy.
     * @return A unique strategy ID.
     */
    public String generateStrategyId(String strategyClass) {
        return strategyClass + "-" + UUID.randomUUID().toString().substring(0, 8).replace("-", "") + "-frontend";
    }
}