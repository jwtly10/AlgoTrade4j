package dev.jwtly10.api.service;

import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.data.CSVDataProvider;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.StrategyStopEvent;
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
    private final ConcurrentHashMap<String, StrategyExecutor> runningStrategies = new ConcurrentHashMap<>();
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

        TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, "SimplePrintStrategy", eventPublisher);

        AccountManager accountManager = new DefaultAccountManager(new Number(10000), new Number(10000), new Number(10000));

        TradeStateManager tradeStateManager = new DefaultTradeStateManager();

        Strategy strategy = new SimpleSMAStrategy();

        StrategyExecutor executor = new StrategyExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher);

        executorService.submit(() -> {
            try {
                executor.run();
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
        StrategyExecutor executor = runningStrategies.get(strategyId);
        if (executor != null) {
            executor.stop();
            runningStrategies.remove(strategyId);
            eventPublisher.publishEvent(new StrategyStopEvent(strategyId, "User requested stop"));
            return true;
        }
        return false;
    }


}