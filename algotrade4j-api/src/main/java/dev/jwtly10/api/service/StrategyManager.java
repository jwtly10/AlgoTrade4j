package dev.jwtly10.api.service;

import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.core.Number;
import dev.jwtly10.core.*;
import dev.jwtly10.core.backtest.BacktestPriceFeed;
import dev.jwtly10.core.datafeed.*;
import dev.jwtly10.core.defaults.DefaultBarSeries;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.StrategyStopEvent;
import dev.jwtly10.core.strategy.SimpleSMAStrategy;
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
        DataFeed dataFeed = createDataFeed(config);
        BarSeries barSeries = new DefaultBarSeries(100);
        PriceFeed priceFeed = new BacktestPriceFeed(barSeries, new Number(10));
        Strategy strategy = createStrategy(config, priceFeed);

        StrategyExecutor executor = new StrategyExecutor(
                strategy,
                priceFeed,
                barSeries,
                dataFeed,
                config.getInitialCash(),
                eventPublisher
        );

        // TODO: Implement dynamically adding indicators or parameters
        // executor.addIndicator(new SomeIndicator());

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

    private Strategy createStrategy(StrategyConfig config, PriceFeed priceFeed) {
        // TODO: This should be loaded by passing in the class name
        return new SimpleSMAStrategy();
//        return new SimplePrintStrategy();
    }

    private DataFeed createDataFeed(StrategyConfig config) {
        CsvParseFormat format = new DefaultCsvFormat(Duration.ofDays(1));
        // TODO: Dont hardcode path, have a specific directory for example data
        return new CsvDataFeed(
                "NAS100_USD",
                "/Users/personal/Projects/AlgoTrade4j/algotrade4j-core/src/main/resources/nas100USD_1D_testdata.csv",
                format,
                DataFeedSpeed.FAST);
    }
}