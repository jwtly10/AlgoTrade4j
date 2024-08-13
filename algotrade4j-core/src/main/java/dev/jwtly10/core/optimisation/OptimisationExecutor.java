package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.CSVDataProvider;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.*;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.SimpleSMAStrategy;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class OptimisationExecutor {
    private final ExecutorService executorService;
    private final OptimisationResultListener resultListener;
    private final EventPublisher eventPublisher;
    private volatile boolean running = false;

    public OptimisationExecutor(EventPublisher eventPublisher) {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.resultListener = new OptimisationResultListener();
        this.eventPublisher = eventPublisher;
        this.eventPublisher.addListener(resultListener);
    }

    public void runOptimisation(OptimisationConfig config) throws Exception {
        running = true;
        // TODO: Generate from config
        List<Map<String, String>> parameterCombinations = generateParameterCombinations(config.getParameterRanges());
        log.info("Generated {} parameter combinations", parameterCombinations.size());
        List<BacktestExecutor> runningStrategies = new ArrayList<>();

        // Shared
        Duration period = Duration.ofDays(1);
        BarSeries barSeries = new DefaultBarSeries("Optimisation", 4000);
        Tick currentTick = new DefaultTick();
        CSVDataProvider dataProvider = new CSVDataProvider(
                "/Users/personal/Projects/AlgoTrade4j/algotrade4j-core/src/main/resources/nas100USD_1D_testdata.csv",
                4,
                new Number(0.1),
                period,
                config.getSymbol()
        );
        dataProvider.setDataSpeed(DataSpeed.INSTANT);
        DefaultDataManager dataManager = new DefaultDataManager(config.getSymbol(), dataProvider, period, barSeries);


        for (Map<String, String> parameterCombination : parameterCombinations) {
            if (!running) break;  // Check if we should stop before creating each strategy
            log.debug("Creating strategy with parameters: {}", parameterCombination);
            // Create a new test strategy id
            String id = "test-" + UUID.randomUUID().toString().substring(0, 8).replace("-", "");

            TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, id, eventPublisher);
            AccountManager accountManager = new DefaultAccountManager(new Number(10000));
            TradeStateManager tradeStateManager = new DefaultTradeStateManager(id, eventPublisher);
            PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();

            SimpleSMAStrategy strategy = new SimpleSMAStrategy(id);
            strategy.setParameters(parameterCombination);
            BacktestExecutor executor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);
            executor.initialise();
            dataManager.addDataListener(executor);

            runningStrategies.add(executor);
        }

        if (running) {
            dataManager.start();

            while (running && dataManager.isRunning()) {
                Thread.sleep(100);
            }

            // Collect results
            for (BacktestExecutor strategy : runningStrategies) {
                if (!running) break;
                var results = resultListener.getResults();
                log.info("Results for strategy {}: {}", strategy.getStrategyId(), results.get(strategy.getStrategyId()).pretty());
            }
        }


    }

    public List<Map<String, String>> generateParameterCombinations(List<ParameterRange> parameterRanges) {
        // TODO: Actually generate this data properly
        List<Map<String, String>> parameterCombinations = new ArrayList<>();

        Map<String, String> combination1 = new HashMap<>();
        combination1.put("smaLength", "10");
        parameterCombinations.add(combination1);

        Map<String, String> combination2 = new HashMap<>();
        combination2.put("smaLength", "20");
        parameterCombinations.add(combination2);

        return parameterCombinations;
    }


}