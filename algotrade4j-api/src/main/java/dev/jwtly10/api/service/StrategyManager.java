package dev.jwtly10.api.service;

import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.CSVDataProvider;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.*;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.BaseStrategy;
import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;
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

    public StrategyManager(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void startStrategy(StrategyConfig config, String strategyId) {
        Duration period = Duration.ofDays(1);
        CSVDataProvider csvDataProvider = new CSVDataProvider(
                "/Users/personal/Projects/AlgoTrade4j/algotrade4j-core/src/main/resources/nas100USD_1D_testdata.csv",
                10,
                new Number(0.1),
                period,
                "NAS100USD"
        );
        csvDataProvider.setDataSpeed(DataSpeed.FAST);

        // TODO: How long does this actually equate too?
        BarSeries barSeries = new DefaultBarSeries(4000);

        DefaultDataManager dataManager = new DefaultDataManager("NAS100USD", csvDataProvider, period, barSeries);

        Tick currentTick = new DefaultTick();

        Strategy strategy = getStrategyFromClassName(config.getStrategyClass(), strategyId);

        TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, strategy.getStrategyId(), eventPublisher);

        AccountManager accountManager = new DefaultAccountManager(new Number(10000));

        TradeStateManager tradeStateManager = new DefaultTradeStateManager(strategy.getStrategyId(), eventPublisher);

        PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();

        BacktestExecutor executor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);
        executor.initialise();
        dataManager.addDataListener(executor);

        executorService.submit(() -> {
            Thread.currentThread().setName("StrategyExecutor-" + strategy.getStrategyId());
            try {
                dataManager.start();
            } catch (Exception e) {
                log.error("Error running strategy", e);
                runningStrategies.remove(strategy.getStrategyId());
                eventPublisher.publishErrorEvent(strategy.getStrategyId(), e);
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
        Strategy strategy = getStrategyFromClassName(strategyClass, null);
        try {
            ParameterHandler.initialize(strategy);
        } catch (IllegalAccessException e) {
            log.error("Error initializing parameters for strategy: {}", strategyClass, e);
            throw new StrategyManagerException("Error initializing parameters for strategy: " + strategyClass, StrategyManagerException.ErrorType.INTERNAL_ERROR);
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
     * Get a strategy instance from a class name.
     *
     * @param className The class name of the strategy.
     * @param customId  A custom ID to use for the strategy.
     * @return A new instance of the strategy.
     */
    private Strategy getStrategyFromClassName(String className, String customId) {
        try {
            Class<?> clazz = Class.forName("dev.jwtly10.core.strategy." + className);
            Strategy strategy;

            if (customId != null) {
                try {
                    Constructor<?> constructor = clazz.getConstructor(String.class);
                    strategy = (Strategy) constructor.newInstance(customId);
                } catch (NoSuchMethodException e) {
                    log.warn("No constructor with String parameter found for {}. Using no-arg constructor.", className);
                    Constructor<?> constructor = clazz.getConstructor();
                    strategy = (Strategy) constructor.newInstance();
                }
            } else {
                // Use the no-arg constructor if customId is null
                Constructor<?> constructor = clazz.getConstructor();
                strategy = (Strategy) constructor.newInstance();
            }

            return strategy;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Error initializing strategy: {}", className, e);
            throw new StrategyManagerException("Error getting strategy from " + className + ": " + e.getClass() + " " + e.getMessage(), StrategyManagerException.ErrorType.INTERNAL_ERROR);
        }
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