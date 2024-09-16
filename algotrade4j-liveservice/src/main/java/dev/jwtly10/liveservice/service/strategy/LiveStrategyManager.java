package dev.jwtly10.liveservice.service.strategy;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.BarEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.DefaultBarSeries;
import dev.jwtly10.core.strategy.DefaultStrategyFactory;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.strategy.StrategyFactory;
import dev.jwtly10.liveservice.executor.LiveExecutor;
import dev.jwtly10.liveservice.executor.LiveStateManager;
import dev.jwtly10.liveservice.executor.LiveTradeManager;
import dev.jwtly10.liveservice.model.LiveStrategy;
import dev.jwtly10.liveservice.model.LiveStrategyConfig;
import dev.jwtly10.liveservice.repository.LiveExecutorRepository;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.common.LiveExternalDataProvider;
import dev.jwtly10.marketdata.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiveStrategyManager {
    private final EventPublisher eventPublisher;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private final LiveExecutorRepository liveExecutorRepository;
    private final OandaClient oandaClient;

    private final LiveStrategyService liveStrategyService;

    public LiveStrategyManager(EventPublisher eventPublisher, LiveExecutorRepository liveExecutorRepository, OandaClient oandaClient, LiveStrategyService liveStrategyService) {
        this.liveExecutorRepository = liveExecutorRepository;
        this.eventPublisher = eventPublisher;
        this.oandaClient = oandaClient;
        this.liveStrategyService = liveStrategyService;
    }

    /**
     * Load all active strategies on application startup
     * <p>
     * This is to ensure that all active strategies are loaded into memory on application startup
     * and are ready to run.
     * This helps to ensure that the strategies are running even if the application is restarted (e.g. after a crash or deployment).
     * </p>
     */
    @PostConstruct
    public void initActiveStrategies() {
        List<LiveStrategy> strategies = liveStrategyService.getActiveLiveStrategies();
        log.info("Found {} active live strategies to run on application init", strategies.size());

        for (LiveStrategy strategy : strategies) {
            try {
                startStrategy(strategy);
            } catch (Exception e) {
                log.error("Error starting strategy", e);
                liveStrategyService.setErrorMessage(strategy, e.getMessage());
                liveStrategyService.deactivateStrategy(strategy.getStrategyName());
            }
        }
    }

    /**
     * Start a live strategy
     *
     * @param strategy The strategy to start
     * @throws Exception error starting the strategy
     */
    public void startStrategy(LiveStrategy strategy) throws Exception {
        if (liveExecutorRepository.containsStrategy(strategy.getStrategyName())) {
            log.warn("Strategy {} is already running", strategy.getStrategyName());
            return;
        }

        log.info("Starting live strategy: {}", strategy.getStrategyName());
        // Reset error msg
        liveStrategyService.setErrorMessage(strategy, null);

        LiveExecutor executor = createExecutor(strategy);
        liveExecutorRepository.addStrategy(strategy.getStrategyName(), executor);

        executorService.submit(() -> {
            Thread.currentThread().setName("LiveStrategyExecutor-" + strategy.getStrategyName());
            try {
                executor.getDataManager().start();
            } catch (Exception e) {
                log.error("Error running strategy", e);
                liveExecutorRepository.removeStrategy(strategy.getStrategyName());
                eventPublisher.publishErrorEvent(strategy.getStrategyName(), e);
            }
        });
    }

    /**
     * Stop a live strategy
     *
     * @param strategyName The name of the strategy to stop
     */
    public void stopStrategy(String strategyName) {
        try {
            LiveExecutor executor = liveExecutorRepository.removeStrategy(strategyName);
            if (executor != null) {
                log.info("Stopping live strategy: {}", strategyName);
                try {
                    executor.getDataManager().stop();
                } catch (Exception e) {
                    log.error("Error stopping strategy", e);
                    log.warn("Attempting to force stop strategy: {}", strategyName);
                    executor.onStop();
                }
            } else {
                log.warn("Strategy {} is not running", strategyName);
            }
        } catch (Exception e) {
            log.error("Error stopping strategy - THIS SHOULD NOT HAPPEN!", e);
        }
    }

    /**
     * Create a new live executor for a strategy
     *
     * @param liveStrategy The live strategy to create the executor for
     * @return The live executor
     * @throws Exception error creating the executor
     */
    private LiveExecutor createExecutor(LiveStrategy liveStrategy) throws Exception {
        final String strategyId = liveStrategy.getStrategyName();
        final LiveStrategyConfig config = liveStrategy.getConfig();
        final StrategyFactory strategyFactory = new DefaultStrategyFactory();
        final OandaBrokerClient client = new OandaBrokerClient(oandaClient, liveStrategy.getBrokerAccount().getAccountId());

        BarSeries barSeries = new DefaultBarSeries(5000);

        // Validates the strategy configuration against the parameters of the strategy class
        config.validate();

        // Create strategy instance
        Strategy strategyInstance = strategyFactory.createStrategy(config.getStrategyClass(), strategyId);
        LiveExternalDataProvider dataProvider = new LiveExternalDataProvider(client, config.getInstrumentData().getInstrument());

        // Preload data to ensure the live strategy 'starts' with enough data for all calculations (indicators) to be valid
        // TODO: This should be based on either indicators or period size (eg we dont need a week of data for 1m period)
        List<DefaultBar> preCandles = client.fetchCandles(config.getInstrumentData().getInstrument(), ZonedDateTime.now().minusDays(7), ZonedDateTime.now(), config.getPeriod().getDuration());
        preCandles.forEach(barSeries::addBar);
        barSeries.getBars().forEach(bar -> eventPublisher.publishEvent(new BarEvent(strategyId, config.getInstrumentData().getInstrument(), bar)));

        DefaultDataManager dataManager = new DefaultDataManager(strategyId, config.getInstrumentData().getInstrument(), dataProvider, config.getPeriod().getDuration(), barSeries, eventPublisher);
        dataManager.initialise(barSeries.getLastBar(), barSeries.getLastBar().getOpenTime().plus(config.getPeriod().getDuration()));

        Map<String, String> runParams = config.getRunParams().stream()
                .collect(Collectors.toMap(LiveStrategyConfig.RunParameter::getName, LiveStrategyConfig.RunParameter::getValue));

        // Init with an empty account
        AccountManager accountManager = new DefaultAccountManager(0, 0, 0);
        TradeManager tradeManager = new LiveTradeManager(client);

        BrokerClient brokerClient = new OandaBrokerClient(oandaClient, liveStrategy.getBrokerAccount().getAccountId());
        LiveStateManager liveStateManager = new LiveStateManager(brokerClient, accountManager, tradeManager, eventPublisher, strategyId, config.getInstrumentData().getInstrument());

        strategyInstance.setParameters(runParams);

        LiveExecutor executor = new LiveExecutor(
                strategyInstance,
                tradeManager,
                accountManager,
                dataManager,
                eventPublisher,
                liveStateManager
        );

        executor.initialise();
        dataManager.addDataListener(executor);

        return executor;
    }

}