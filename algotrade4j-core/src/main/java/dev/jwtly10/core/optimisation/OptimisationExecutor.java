package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.data.DataManagerFactory;
import dev.jwtly10.core.data.DataProvider;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.AnalysisEvent;
import dev.jwtly10.core.exception.BacktestExecutorException;
import dev.jwtly10.core.execution.BacktestExecutor;
import dev.jwtly10.core.execution.ExecutorFactory;
import dev.jwtly10.core.external.news.StrategyNewsUtil;
import dev.jwtly10.core.model.Broker;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.strategy.StrategyFactory;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Executor for running the optimisation process.
 * This class is responsible for generating parameter combinations, running strategies, and collecting results.
 * The optimisation process is run in batches to avoid running too many strategies at once.
 * The results and progress updates are via emitted via callbacks
 */
@Slf4j
public class OptimisationExecutor {
    private final OptimisationResultListener resultListener;
    private final Map<String, Map<String, String>> strategyParameters = new HashMap<>();

    private final DataProvider dataProvider;
    private final EventPublisher eventPublisher;
    private final Consumer<OptimisationRunResult> resultCallback;
    private final Consumer<OptimisationProgress> progressCallback;
    private final List<String> failedStrategyIds = new CopyOnWriteArrayList<>();
    private final StrategyFactory strategyFactory;
    private final ExecutorFactory executorFactory;
    private final DataManagerFactory dataManagerFactory;
    private final StrategyNewsUtil strategyNewsUtil;
    private final Broker BROKER;
    private OptimisationProgress progress;
    private volatile boolean running = false;


    /**
     * @param eventPublisher   the event publisher that handles all events emitted by the strategies ran during backtesting
     * @param dataProvider     the data provider that handles data
     * @param resultCallback   a Consumer that allows callers to handle new results that have been emitted during optimisation
     * @param progressCallback a Consumre that allows callers to handle progress updates once each batch has been processed
     */
    public OptimisationExecutor(
            Broker broker,
            EventPublisher eventPublisher,
            DataProvider dataProvider,
            Consumer<OptimisationRunResult> resultCallback,
            Consumer<OptimisationProgress> progressCallback,
            StrategyFactory strategyFactory,
            ExecutorFactory executorFactory,
            DataManagerFactory dataManagerFactory,
            StrategyNewsUtil strategyNewsUtil
    ) {
        this.BROKER = broker;
        this.resultCallback = resultCallback;
        this.progressCallback = progressCallback;
        this.resultListener = new OptimisationResultListener(this);
        this.eventPublisher = eventPublisher;
        this.eventPublisher.addListener(resultListener);
        this.dataProvider = dataProvider;
        this.strategyFactory = strategyFactory;
        this.executorFactory = executorFactory;
        this.dataManagerFactory = dataManagerFactory;
        this.strategyNewsUtil = strategyNewsUtil;
    }

    /**
     * Run the optimisation process with the given configuration.
     *
     * @param config The optimisation configuration to use.
     * @throws Exception If an error occurs during the optimisation process.
     */
    public void executeTask(OptimisationConfig config) throws Exception {
        long startTime = System.nanoTime();
        log.info("Optimisation Config: {}", config);

        running = true;

        List<Map<String, String>> parameterCombinations = generateParameterCombinations(config.getParameterRanges());
        log.info("Generated {} parameter combinations", parameterCombinations.size());

        this.progress = new OptimisationProgress(parameterCombinations.size());
        progressCallback.accept(progress);

        for (int i = 1; i < Math.min(parameterCombinations.size(), 7); i++) {
            log.info("Parameter combo {}: {}", i, parameterCombinations.get(i - 1));
        }

        if (parameterCombinations.isEmpty()) {
            log.warn("No parameter combinations to optimise");
            throw new RuntimeException("No parameter combinations to optimise");
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
            completedRuns += batch.size();

            processBatchResults(batchExecutors);
            progress.updateProgress(completedRuns);
            progressCallback.accept(progress);

            log.info("Completed {}/{} runs. {} remaining",
                    completedRuns,
                    parameterCombinations.size(),
                    parameterCombinations.size() - completedRuns);
        }

        log.info("Optimisation Complete");

        long endTime = System.nanoTime();
        long durationInNanos = endTime - startTime;
        long durationInSeconds = durationInNanos / 1_000_000_000;

        long minutes = durationInSeconds / 60;
        long seconds = durationInSeconds % 60;

        log.info("Total execution time: {} minutes and {} seconds", minutes, seconds);
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

        // Ensure instant
        dataProvider.setDataSpeed(DataSpeed.INSTANT);
        DefaultDataManager dataManager = (DefaultDataManager) dataManagerFactory.createDataManager(instrument, period, eventPublisher, dataProvider);

        dataManager.setIsOptimising(true);

        for (Map<String, String> parameterCombination : batch) {
            if (!running) break;
            log.debug("Creating strategy with parameters: {}", parameterCombination);

            String id = "optimisation-" + config.getStrategyClass() + "-" + UUID.randomUUID().toString().substring(0, 8).replace("-", "");
            strategyParameters.put(id, new HashMap<>(parameterCombination));

            Strategy strategy = null;
            try {
                strategy = strategyFactory.createStrategy(config.getStrategyClass(), id);
            } catch (Exception e) {
                // Context
                throw new Exception("Error getting strategy from " + config.getStrategyClass() + ": " + e.getClass() + " " + e.getMessage(), e);
            }

            try {
                ParameterHandler.validateRunParameters(strategy, parameterCombination);
                strategy.setParameters(parameterCombination);
                BacktestExecutor executor = executorFactory.createExecutor(BROKER, strategy, id, dataManager, eventPublisher, strategyNewsUtil, config.getInitialCash());
                executor.initialise();
                dataManager.addDataListener(executor);

                batchExecutors.add(executor);
            } catch (Exception e) {
                log.error("Failed to initialise strategy '{}'", strategy.getStrategyId(), e);
                eventPublisher.publishErrorEvent(strategy.getStrategyId(), e);
            }
        }

        if (running) {
            dataManager.start();

            while (running && dataManager.isRunning()) {
                Thread.sleep(100);
            }
        }

        // Clean up
        dataManager.stop("End of optimisation");
        for (BacktestExecutor executor : batchExecutors) {
            dataManager.removeDataListener(executor);
        }
        return batchExecutors;
    }

    /**
     * Processes a batch of executors to get results and emit to callbacks
     *
     * @param batchExecutors the executors that were being run
     */
    private void processBatchResults(List<BacktestExecutor> batchExecutors) {
        for (BacktestExecutor executor : batchExecutors) {
            String strategyId = executor.getStrategyId();
            AnalysisEvent res = resultListener.getResults().get(strategyId);
            Map<String, String> params = strategyParameters.get(strategyId);

            StrategyOutput strategyOutput = new StrategyOutput();
            strategyOutput.setStrategyId(strategyId);

            if (res != null) {
                strategyOutput.setFailed(false);
                strategyOutput.setStats(res.getStats());
                OptimisationRunResult result = new OptimisationRunResult(strategyId, params, strategyOutput);
                resultCallback.accept(result);
            } else {
                // If there is no stats for a strategy, we can assume that it failed.
                // and That it was handled by 'onStrategyFailure', so we can do nothing.
                // else lets be sure and save generic message this:
                if (!failedStrategyIds.contains(strategyId)) {
                    log.warn("A strategy did not contain a result but was not counted as failed: {}", executor);
                    strategyOutput.setFailed(true);
                    strategyOutput.setReason("Strategy execution failed or produced no results");
                    OptimisationRunResult result = new OptimisationRunResult(strategyId, params, strategyOutput);
                    resultCallback.accept(result);
                }
            }
        }
    }


    /**
     * Emits on error event to callback
     *
     * @param strategyId the strategyID this failure is for
     * @param e          the exception
     */
    public void onStrategyFailure(String strategyId, Exception e) {
        StrategyOutput strategyOutput = new StrategyOutput();
        strategyOutput.setStrategyId(strategyId);
        strategyOutput.setFailed(true);
        failedStrategyIds.add(strategyId);

        String reason;
        if (e instanceof BacktestExecutorException) {
            Throwable cause = e.getCause();
            reason = Objects.requireNonNullElse(cause, e).getMessage();
        } else {
            reason = e.getMessage();
        }
        strategyOutput.setReason(reason);

        Map<String, String> params = strategyParameters.get(strategyId);
        OptimisationRunResult result = new OptimisationRunResult(strategyId, params, strategyOutput);
        resultCallback.accept(result);
    }


    /**
     * Generate all possible combinations of the given parameter ranges.
     *
     * @param parameterRanges The parameter ranges to generate combinations for.
     * @return A list of all possible parameter combinations, ready to be used in optimisation.
     * @throws IllegalArgumentException if parameters are not valid.
     */
    public List<Map<String, String>> generateParameterCombinations(List<ParameterRange> parameterRanges) throws IllegalArgumentException {
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

        try {
            if (!range.getSelected()) {
                // Use the realtime value configured, since we are not optimising for this parameter
                currentCombination.put(range.getName(), range.getValue());
                generateCombinationsRecursive(parameterRanges, index + 1, currentCombination, combinations);
            } else if (range.getStringList() != null && !range.getStringList().isEmpty()) { // Only do this logic if the string list is not empty
                // This is an enum or string parameter, so we parse the string list and generate combinations
                String[] values = range.getStringList().split(",");
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }

                for (String value : values) {
                    currentCombination.put(range.getName(), value);
                    generateCombinationsRecursive(parameterRanges, index + 1, currentCombination, combinations);
                }
            } else {
                // Number is used here, as we often get rounding precision errors when using doubles directly
                Number start = new Number(Double.parseDouble(range.getStart()));
                Number end = new Number(Double.parseDouble(range.getEnd()));
                Number step = new Number(Double.parseDouble(range.getStep()));

                if (start.equals(end)) {
                    currentCombination.put(range.getName(), String.valueOf(start));
                    generateCombinationsRecursive(parameterRanges, index + 1, currentCombination, combinations);
                } else {
                    for (Number value = start; value.compareTo(end) <= 0; value = value.add(step)) {
                        currentCombination.put(range.getName(), String.valueOf(value.doubleValue()));
                        generateCombinationsRecursive(parameterRanges, index + 1, currentCombination, combinations);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid parameter range: " + range, e);
        }
    }
}