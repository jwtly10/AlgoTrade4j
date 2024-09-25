package dev.jwtly10.backtestapi.jobs;

import dev.jwtly10.backtestapi.model.optimisation.OptimisationState;
import dev.jwtly10.backtestapi.model.optimisation.OptimisationTask;
import dev.jwtly10.backtestapi.model.optimisation.ProgressInfo;
import dev.jwtly10.backtestapi.service.optimisation.OptimisationResultService;
import dev.jwtly10.backtestapi.service.optimisation.OptimisationTaskService;
import dev.jwtly10.core.data.DataManagerFactory;
import dev.jwtly10.core.data.DataProvider;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.SyncEventPublisher;
import dev.jwtly10.core.execution.ExecutorFactory;
import dev.jwtly10.core.optimisation.OptimisationConfig;
import dev.jwtly10.core.optimisation.OptimisationExecutor;
import dev.jwtly10.core.optimisation.OptimisationProgress;
import dev.jwtly10.core.optimisation.OptimisationRunResult;
import dev.jwtly10.core.strategy.StrategyFactory;
import dev.jwtly10.marketdata.common.ExternalDataClient;
import dev.jwtly10.marketdata.common.ExternalDataProvider;
import dev.jwtly10.marketdata.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import dev.jwtly10.marketdata.oanda.OandaDataClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

@Service
@Slf4j
public class OptimisationBackgroundJob {
    private final OptimisationTaskService taskService;
    private final OptimisationResultService resultService;
    private final Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore taskSemaphore;

    private final StrategyFactory strategyFactory;
    private final ExecutorFactory executorFactory;
    private final DataManagerFactory dataManagerFactory;

    private final OandaClient oandaClient;

    public OptimisationBackgroundJob(
            OptimisationTaskService taskService,
            OptimisationResultService resultService,
            @Value("${optimisation.max.concurrent.tasks:1}") int maxConcurrentTasks,
            StrategyFactory strategyFactory,
            ExecutorFactory executorFactory,
            DataManagerFactory dataManagerFactory,
            OandaClient oandaClient
    ) {
        this.taskService = taskService;
        this.resultService = resultService;
        this.taskSemaphore = new Semaphore(maxConcurrentTasks);
        this.strategyFactory = strategyFactory;
        this.executorFactory = executorFactory;
        this.dataManagerFactory = dataManagerFactory;
        this.oandaClient = oandaClient;
    }


    @Scheduled(fixedDelayString = "${optimisation.job.delay:60000}")
    public void processPendingOptimisationTasks() {
        log.trace("Running Optimisation Job");
        if (!taskSemaphore.tryAcquire()) {
            log.info("Maximum number of concurrent tasks reached. Skipping this run.");
            return;
        }

        try {
            OptimisationTask pendingTask = taskService.findFirstByState(OptimisationState.PENDING);
            if (pendingTask != null) {
                log.info("Found pending optimisation task id: {}", pendingTask.getId());
                processTask(pendingTask);
            } else {
                log.trace("No pending tasks found.");
                taskSemaphore.release();  // Release if no task was processed
            }
        } catch (Exception e) {
            log.error("Error processing optimisation task", e);
            taskSemaphore.release();  // Ensure semaphore is released on error
        }
    }

    private void processTask(OptimisationTask task) {
        log.info("Processing optimisation task id: {}", task.getId());

        try {
            task.setState(OptimisationState.RUNNING);
            taskService.save(task);

            CompletableFuture.runAsync(() -> {
                Thread.currentThread().setName("OptimisationExecutor-" + task.getId());
                try {
                    runOptimisation(task.getConfig(), task);
                    completeTask(task);
                } catch (Exception e) {
                    handleOptimisationError(task, e);
                } finally {
                    taskSemaphore.release();
                }
            }, virtualThreadExecutor);
        } catch (Exception e) {
            handleOptimisationError(task, e);
            taskSemaphore.release();
        }
    }

    private void completeTask(OptimisationTask task) {
        task.setState(OptimisationState.COMPLETED);
        taskService.save(task);
    }

    private void runOptimisation(OptimisationConfig config, OptimisationTask task) throws Exception {
        OandaBrokerClient oandaBrokerClient = new OandaBrokerClient(oandaClient, null);
        ExternalDataClient externalDataClient = new OandaDataClient(oandaBrokerClient);

        ZonedDateTime from = config.getTimeframe().getFrom().withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime to = config.getTimeframe().getTo().withZoneSameInstant(ZoneId.of("UTC"));
        DataProvider dataProvider = new ExternalDataProvider(externalDataClient, config.getInstrument(), config.getSpread(), config.getPeriod(), from, to, 12345L);

        OptimisationExecutor optimisationExecutor = getOptimisationExecutor(task, dataProvider);
        optimisationExecutor.executeTask(config);
    }

    private @NotNull OptimisationExecutor getOptimisationExecutor(OptimisationTask task, DataProvider dataProvider) {
        EventPublisher internalEventPublisher = new SyncEventPublisher();

        Consumer<OptimisationRunResult> resultCallback = runResult -> {
            resultService.saveOptimisationResult(task.getId(), runResult);
        };

        Consumer<OptimisationProgress> progressCallback = progress -> {
            taskService.updateProgress(task.getId(), new ProgressInfo(
                    progress.getProgressPercentage(),
                    progress.getCompletedTasks(),
                    progress.getRemainingTasks(),
                    progress.getEstimatedTimeRemaining()
            ));
        };

        return new OptimisationExecutor(
                internalEventPublisher,
                dataProvider,
                resultCallback,
                progressCallback,
                strategyFactory,
                executorFactory,
                dataManagerFactory
        );
    }

    private void handleOptimisationError(OptimisationTask task, Exception e) {
        log.error("Failed to run optimisation for task {}", task.getId(), e);
        task.setState(OptimisationState.FAILED);
        task.setErrorMessage(e.getMessage());
        taskService.save(task);
    }
}