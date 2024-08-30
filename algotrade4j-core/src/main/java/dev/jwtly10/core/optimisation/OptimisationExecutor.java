package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataProvider;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.AnalysisEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.exception.BacktestExecutorException;
import dev.jwtly10.core.execution.*;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.utils.StrategyReflectionUtils;
import lombok.extern.slf4j.Slf4j;

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
    private final Map<String, Map<String, String>> strategyParameters = new HashMap<>();
    private final List<OptimisationResult.FailedStrategy> failedStrategies = new ArrayList<>();
    private final List<BacktestExecutor> allExecutors = new ArrayList<>();

    private final DataProvider dataProvider;
    private final EventPublisher eventPublisher;
    private volatile boolean running = false;

    public OptimisationExecutor(EventPublisher eventPublisher, DataProvider dataProvider) {
        this.resultListener = new OptimisationResultListener(this);
        this.eventPublisher = eventPublisher;
        this.eventPublisher.addListener(resultListener);
        this.dataProvider = dataProvider;
    }

    /**
     * Run the optimisation process with the given configuration.
     *
     * @param config The optimisation configuration to use.
     * @throws Exception If an error occurs during the optimisation process.
     */
    public OptimisationResult runOptimisation(OptimisationConfig config) throws Exception {
        log.info("Optimisation Config: {}", config);

        OptimisationResult optimisationResult = new OptimisationResult();
        running = true;

        List<Map<String, String>> parameterCombinations = generateParameterCombinations(config.getParameterRanges());
        log.info("Generated {} parameter combinations", parameterCombinations.size());

        for (int i = 1; i < Math.min(parameterCombinations.size(), 7); i++) {
            log.info("Parameter combo {}: {}", i, parameterCombinations.get(i - 1));
        }

        // TODO: Add more validation
        if (parameterCombinations.isEmpty()) {
            log.warn("No parameter combinations to optimise");
            return optimisationResult;
        }

        // TODO: Limited to 1000 for now, just to avoid running too many strategies at once, before we optimise/stress test
        if (parameterCombinations.size() > 1000) {
            log.warn("This is supported, and should be pretty quick, but just waiting to see at what point this becomes a factor");
            throw new RuntimeException("Too many parameter combinations. We dont support > 1000 yet");
        }

        int batchSize = 50;

        int completedRuns = 0;
        for (int i = 0; i < parameterCombinations.size(); i += batchSize) {
            if (!running) break;

            int endIndex = Math.min(i + batchSize, parameterCombinations.size());
            List<Map<String, String>> batch = parameterCombinations.subList(i, endIndex);

            log.info("Processing batch {}/{}. Combinations {}-{} out of {}",
                    (i / batchSize) + 1,
                    (parameterCombinations.size() + batchSize - 1) / batchSize,
                    i + 1,
                    endIndex,
                    parameterCombinations.size());

            List<BacktestExecutor> batchExecutors = processBatch(batch, config);
            allExecutors.addAll(batchExecutors);
            completedRuns += batch.size();

            log.info("Completed {}/{} runs. {} remaining",
                    completedRuns,
                    parameterCombinations.size(),
                    parameterCombinations.size() - completedRuns);
        }

        // Collect all results at the end
        if (running) {
            optimisationResult = collectResults(allExecutors);
        }

        if (!failedStrategies.isEmpty()) {
            log.warn("The following strategies encountered errors during optimization: {}", failedStrategies);
            optimisationResult.setFailures(failedStrategies);
        }

        log.info("Success: {}, Failures: {}", optimisationResult.getSuccessfulStrategies().size(), optimisationResult.getFailedStrategies().size());

        return optimisationResult;
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
        Instrument instrument = config.getInstrument();

        // Shared deps
        BarSeries barSeries = new DefaultBarSeries("Optimisation", 4000);
        Tick currentTick = new DefaultTick();

        // Ensure instant
        dataProvider.setDataSpeed(DataSpeed.INSTANT);
        DefaultDataManager dataManager = new DefaultDataManager("optimisation-run", instrument, dataProvider, period, barSeries, eventPublisher);
        dataManager.setIsOptimising(true);

        for (Map<String, String> parameterCombination : batch) {
            if (!running) break;
            log.debug("Creating strategy with parameters: {}", parameterCombination);

            String id = "optimisation-" + config.getStrategyClass() + "-" + UUID.randomUUID().toString().substring(0, 8).replace("-", "");
            strategyParameters.put(id, new HashMap<>(parameterCombination));
            log.info("Strategy parameters: {}", strategyParameters);

            TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, id, eventPublisher);
            AccountManager accountManager = new DefaultAccountManager(config.getInitialCash(), config.getInitialCash(), config.getInitialCash());
            TradeStateManager tradeStateManager = new DefaultTradeStateManager(id, eventPublisher);
            PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();

            Strategy strategy = null;
            try {
                // use the generated id for this specific parameter combo
                strategy = StrategyReflectionUtils.getStrategyFromClassName(config.getStrategyClass(), id);
            } catch (Exception e) {
                // Adding more context to error message
                throw new Exception("Error getting strategy from " + config.getStrategyClass() + ": " + e.getClass() + " " + e.getMessage(), e);
            }

            strategy.setParameters(parameterCombination);
            BacktestExecutor executor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);
            executor.initialise();
            dataManager.addDataListener(executor);

            batchExecutors.add(executor);
        }

        if (running) {
            dataManager.start();

            while (running && dataManager.isRunning()) {
                Thread.sleep(100);
            }
        }

        // Clean up
        dataManager.stop();
        for (BacktestExecutor executor : batchExecutors) {
            dataManager.removeDataListener(executor);
        }
        return batchExecutors;
    }

    /**
     * Collect the results for all strategies in the given list.
     *
     * @param allExecutors The list of executors to collect results for.
     */
    private OptimisationResult collectResults(List<BacktestExecutor> allExecutors) {
        log.debug("Collecting results for {} strategies", allExecutors.size());
        OptimisationResult results = new OptimisationResult();
        for (BacktestExecutor executor : allExecutors) {
            if (!running) break;
            String strategyId = executor.getStrategyId();
            AnalysisEvent event = resultListener.getResults().get(strategyId);
            if (event != null) {
                Map<String, String> params = strategyParameters.get(strategyId);
                var res = new OptimisationResult.SuccessfulStrategy(strategyId, event.getStats(), params);
                results.addSuccess(res);
            }
        }

        return results;
    }

    public void onStrategyFailure(String strategyId, Exception e) {
        var params = strategyParameters.get(strategyId);
        String reason;
        if (e instanceof BacktestExecutorException) {
            Throwable cause = e.getCause();
            reason = Objects.requireNonNullElse(cause, e).getMessage();
        } else {
            reason = e.getMessage();
        }
        failedStrategies.add(new OptimisationResult.FailedStrategy(strategyId, reason, params));
    }

    /**
     * Generate all possible combinations of the given parameter ranges.
     *
     * @param parameterRanges The parameter ranges to generate combinations for.
     * @return A list of all possible parameter combinations, ready to be used in optimisation.
     * @throws IllegalArgumentException if parameters are not valid.
     */
    public List<Map<String, String>> generateParameterCombinations(List<ParameterRange> parameterRanges) throws IllegalArgumentException {
        // TODO: Support the enum type parameters
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

        if (!range.getSelected()) {
            // Use the realtime value configured, since we are not optimising for this parameter
            currentCombination.put(range.getName(), range.getValue());
            generateCombinationsRecursive(parameterRanges, index + 1, currentCombination, combinations);
        } else {
            Number start = new Number(range.getStart());
            Number end = new Number(range.getEnd());
            Number step = new Number(range.getStep());

            if (start.equals(end)) {
                currentCombination.put(range.getName(), start.toString());
                generateCombinationsRecursive(parameterRanges, index + 1, currentCombination, combinations);
            } else {
                for (Number value = start; value.compareTo(end) <= 0; value = value.add(step)) {
                    currentCombination.put(range.getName(), value.toString());
                    generateCombinationsRecursive(parameterRanges, index + 1, currentCombination, combinations);
                }
            }

        }
    }
}