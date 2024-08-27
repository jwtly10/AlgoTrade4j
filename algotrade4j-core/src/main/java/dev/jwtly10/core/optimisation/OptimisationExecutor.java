package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.event.AnalysisEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.*;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;

/**
 * Executor for running the optimisation process.
 * This class is responsible for generating parameter combinations, running strategies, and collecting results.
 * The optimisation process is run in batches to avoid running too many strategies at once.
 * The results are collected at the end and triggered to the event publisher
 */
@Slf4j
public class OptimisationExecutor {
    private final OptimisationResultListener resultListener;
    private final EventPublisher eventPublisher;
    private final Map<String, Map<String, String>> strategyParameters = new HashMap<>();
    private volatile boolean running = false;

    public OptimisationExecutor(EventPublisher eventPublisher) {
        this.resultListener = new OptimisationResultListener();
        this.eventPublisher = eventPublisher;
        this.eventPublisher.addListener(resultListener);
    }

    /**
     * Run the optimisation process with the given configuration.
     *
     * @param config The optimisation configuration to use.
     * @throws Exception If an error occurs during the optimisation process.
     */
    public List<OptimisationResult> runOptimisation(OptimisationConfig config) throws Exception {
        List<OptimisationResult> results = new ArrayList<>();

        running = true;
        List<Map<String, String>> parameterCombinations = generateParameterCombinations(config.getParameterRanges());
        log.info("Generated {} parameter combinations", parameterCombinations.size());

        // TODO: Add more validation
        if (parameterCombinations.isEmpty()) {
            log.warn("No parameter combinations to optimise");
            return List.of();
        }

        // TODO: Limited to 1000 for now, just to avoid running too many strategies at once, before we optimise/stress test
        if (parameterCombinations.size() > 1000) {
            log.warn("This is supported, and should be pretty quick, but just waiting to see at what point this becomes a factor");
            return List.of();
        }

        List<BacktestExecutor> allExecutors = new ArrayList<>();
        int batchSize = 30;

        for (int i = 0; i < parameterCombinations.size(); i += batchSize) {
            if (!running) break;

            int endIndex = Math.min(i + batchSize, parameterCombinations.size());
            List<Map<String, String>> batch = parameterCombinations.subList(i, endIndex);

            List<BacktestExecutor> batchExecutors = processBatch(batch, config);
            allExecutors.addAll(batchExecutors);
        }

        // Collect all results at the end
        if (running) {
            results = collectResults(allExecutors);
        }

        return results;
    }

    /**
     * Process a batch of strategies with the given parameter combinations.
     *
     * @param batch  The batch of parameter combinations to process.
     * @param config The optimisation configuration.
     * @return A list of executors for the strategies in the batch.
     */
    private List<BacktestExecutor> processBatch(List<Map<String, String>> batch, OptimisationConfig config) throws Exception {
        List<BacktestExecutor> batchExecutors = new ArrayList<>();

        Duration period = config.getPeriod();
        Number spread = config.getSpread();
        Instrument instrument = config.getInstrument();
        DataSpeed speed = config.getSpeed();

        // Shared deps
        BarSeries barSeries = new DefaultBarSeries("Optimisation", 4000);
        Tick currentTick = new DefaultTick();
//        CSVDataProvider dataProvider = new CSVDataProvider(
//                "/Users/personal/Projects/AlgoTrade4j/algotrade4j-core/src/main/resources/nas100USD_1D_testdata.csv",
//                4,
//                spread,
//                period,
//                instrument
//        );
        // TODO: Should this always be instant?
//        dataProvider.setDataSpeed(speed);
//        dataProvider.setDataSpeed(DataSpeed.INSTANT);
//
//        DefaultDataManager dataManager = new DefaultDataManager("TODO", instrument, dataProvider, period, barSeries, eventPublisher);

        for (Map<String, String> parameterCombination : batch) {
            if (!running) break;
            log.debug("Creating strategy with parameters: {}", parameterCombination);

            String id = "test-" + UUID.randomUUID().toString().substring(0, 8).replace("-", "");
            strategyParameters.put(id, new HashMap<>(parameterCombination));

            TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, id, eventPublisher);
            AccountManager accountManager = new DefaultAccountManager(config.getInitialCash());
            TradeStateManager tradeStateManager = new DefaultTradeStateManager(id, eventPublisher);
            PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();

            Strategy strategy = getStrategyFromClassName(config.getStrategyClass(), id);
            strategy.setParameters(parameterCombination);
//            BacktestExecutor executor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);
//            executor.initialise();
////            dataManager.addDataListener(executor);
//
//            batchExecutors.add(executor);
        }

        if (running) {
//            dataManager.start();
//
//            while (running && dataManager.isRunning()) {
//                Thread.sleep(100);
//            }
        }

        // Clean up
//        dataManager.stop();
//        for (BacktestExecutor executor : batchExecutors) {
//            dataManager.removeDataListener(executor);
//        }
//
        return batchExecutors;
    }

    /**
     * Collect the results for all strategies in the given list.
     *
     * @param allExecutors The list of executors to collect results for.
     */
    private List<OptimisationResult> collectResults(List<BacktestExecutor> allExecutors) {
        log.debug("Collecting results for {} strategies", allExecutors.size());
        List<OptimisationResult> optimisationResults = new ArrayList<>();
        for (BacktestExecutor executor : allExecutors) {
            if (!running) break;
            String strategyId = executor.getStrategyId();
            AnalysisEvent event = resultListener.getResults().get(strategyId);
            if (event != null) {
                Map<String, String> params = strategyParameters.get(strategyId);
                OptimisationResult result = new OptimisationResult(strategyId, event.getStats(), params);
                optimisationResults.add(result);
                log.info("Results for strategy {}: {}", strategyId, result.getStats());
            }
        }

        return optimisationResults;
    }

    /**
     * Generate all possible combinations of the given parameter ranges.
     *
     * @param parameterRanges The parameter ranges to generate combinations for.
     * @return A list of all possible parameter combinations, ready to be used in optimisation.
     */
    public List<Map<String, String>> generateParameterCombinations(List<ParameterRange> parameterRanges) {
        List<Map<String, String>> combinations = new ArrayList<>();
        generateCombinationsRecursive(parameterRanges, 0, new HashMap<>(), combinations);
        return combinations;
    }

    /**
     * Recursively generate all possible combinations of the given parameter ranges.
     *
     * @param parameterRanges    The parameter ranges to generate combinations for.
     * @param index              The current index in the parameter ranges list.
     * @param currentCombination The current combination of parameters.
     * @param combinations       The list to add the generated combinations to.
     */
    private void generateCombinationsRecursive(List<ParameterRange> parameterRanges, int index,
                                               Map<String, String> currentCombination,
                                               List<Map<String, String>> combinations) {
        if (index == parameterRanges.size()) {
            combinations.add(new HashMap<>(currentCombination));
            return;
        }

        ParameterRange range = parameterRanges.get(index);
        Number start = new Number(range.getStart());
        Number end = new Number(range.getEnd());
        Number step = new Number(range.getStep());

        for (Number value = start; value.compareTo(end) <= 0; value = value.add(step)) {
            currentCombination.put(range.getName(), value.toString());
            generateCombinationsRecursive(parameterRanges, index + 1, currentCombination, combinations);
        }
    }

    /**
     * Get a strategy instance from a class name.
     *
     * @param className The class name of the strategy.
     * @param customId  A custom ID to use for the strategy.
     * @return A new instance of the strategy.
     */
    // TODO: We duplicate this... fix that
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
            throw new IllegalArgumentException("Error initializing strategy: " + className);
        }
    }
}