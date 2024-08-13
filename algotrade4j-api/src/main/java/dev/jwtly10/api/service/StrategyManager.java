package dev.jwtly10.api.service;

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
import dev.jwtly10.core.strategy.SimpleSMAStrategy;
import dev.jwtly10.core.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class StrategyManager {
    private final EventPublisher eventPublisher;
    private final ConcurrentHashMap<String, BacktestExecutor> runningStrategies = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public StrategyManager(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public String startStrategy(StrategyConfig config) {
        Duration period = Duration.ofDays(1);
        CSVDataProvider csvDataProvider = new CSVDataProvider(
                "/Users/personal/Projects/AlgoTrade4j/algotrade4j-core/src/main/resources/nas100USD_1D_testdata.csv",
                4,
                new Number(0.1),
                period,
                "NAS100USD"
        );
        csvDataProvider.setDataSpeed(DataSpeed.FAST);

        BarSeries barSeries = new DefaultBarSeries(4000);

        DefaultDataManager dataManager = new DefaultDataManager("NAS100USD", csvDataProvider, period, barSeries);

        Tick currentTick = new DefaultTick();

        Strategy strategy = new SimpleSMAStrategy();

        TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, strategy.getStrategyId(), eventPublisher);

        AccountManager accountManager = new DefaultAccountManager(new Number(10000));

        TradeStateManager tradeStateManager = new DefaultTradeStateManager(strategy.getStrategyId(), eventPublisher);

        PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();

        BacktestExecutor executor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);
        executor.initialise();
        dataManager.addDataListener(executor);

        executorService.submit(() -> {
            try {
                dataManager.start();
            } catch (Exception e) {
                log.error("Error running strategy", e);
                runningStrategies.remove(strategy.getStrategyId());
                eventPublisher.publishErrorEvent(strategy.getStrategyId(), e);
            }
        });

        runningStrategies.put(strategy.getStrategyId(), executor);
        return strategy.getStrategyId();
    }

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

}