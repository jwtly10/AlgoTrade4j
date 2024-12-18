package dev.jwtly10.liveapi.service.strategy;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.external.news.StrategyNewsUtil;
import dev.jwtly10.core.external.news.forexfactory.ForexFactoryClient;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.DefaultBarSeries;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.core.risk.RiskManagementService;
import dev.jwtly10.core.strategy.DefaultStrategyFactory;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.strategy.StrategyFactory;
import dev.jwtly10.liveapi.executor.LiveExecutor;
import dev.jwtly10.liveapi.executor.LiveStateManager;
import dev.jwtly10.liveapi.executor.LiveTradeManager;
import dev.jwtly10.liveapi.model.broker.BrokerAccount;
import dev.jwtly10.liveapi.model.strategy.LiveStrategy;
import dev.jwtly10.liveapi.model.strategy.LiveStrategyConfig;
import dev.jwtly10.liveapi.repository.LiveExecutorRepository;
import dev.jwtly10.liveapi.service.broker.BrokerClientFactory;
import dev.jwtly10.liveapi.service.risk.LiveRiskManager;
import dev.jwtly10.liveapi.service.risk.RiskManagementServiceClient;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.common.ClientCallback;
import dev.jwtly10.marketdata.common.LiveExternalDataProvider;
import dev.jwtly10.marketdata.impl.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.impl.oanda.OandaClient;
import dev.jwtly10.marketdata.impl.oanda.OandaDataClient;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import dev.jwtly10.shared.service.external.telegram.TelegramNotifier;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    private final TelegramNotifier telegramNotifier;
    private final LiveStrategyService liveStrategyService;

    private final BrokerClientFactory brokerClientFactory;
    private final ForexFactoryClient forexFactoryClient;
    private final RiskManagementServiceClient riskManagementServiceClient;

    public LiveStrategyManager(EventPublisher eventPublisher,
                               LiveExecutorRepository liveExecutorRepository,
                               OandaClient oandaClient,
                               TelegramNotifier telegramNotifier,
                               LiveStrategyService liveStrategyService,
                               BrokerClientFactory brokerClientFactory,
                               ForexFactoryClient forexFactoryClient,
                               RiskManagementServiceClient riskManagementServiceClient
    ) {
        this.liveExecutorRepository = liveExecutorRepository;
        this.eventPublisher = eventPublisher;
        this.oandaClient = oandaClient;
        this.telegramNotifier = telegramNotifier;
        this.liveStrategyService = liveStrategyService;
        this.brokerClientFactory = brokerClientFactory;
        this.forexFactoryClient = forexFactoryClient;
        this.riskManagementServiceClient = riskManagementServiceClient;
    }

    /**
     * Initialise live strategies
     * <p>
     * On application startup, we validate that all strategies in the db are up to date.
     * We check that the configuration stored in db is compatible with the strategy class.
     * If not compatible, we deactivate the strategy and notify the user.
     * </p>
     * <p>
     * Otherwise, we load all active strategies on application startup, and start them.
     * </p>
     */
    @PostConstruct
    public void initLiveStrategies() {
        log.info("Initialising live strategies on application startup");

        List<LiveStrategy> allStrategies = liveStrategyService.getNonHiddenLiveStrategies();
        log.info("Found {} live strategies in the database to validate: {}", allStrategies.size(), allStrategies.stream().map(LiveStrategy::getStrategyName).collect(Collectors.toList()));
        List<LiveStrategy> activeStrategies = new ArrayList<>();
        for (LiveStrategy strategy : allStrategies) {
            try {
                strategy.validateConfigAgainstStrategyClass();
                if (strategy.isActive()) {
                    activeStrategies.add(strategy);
                }
            } catch (Exception e) {
                log.warn("Strategy '{}' failed validation: {}", strategy.getStrategyName(), e.getMessage());
                liveStrategyService.setErrorMessage(strategy, e.getMessage());
                liveStrategyService.deactivateStrategy(strategy.getStrategyName());
            }
        }

        log.info("Found {} active live strategies to run on application init: {}", activeStrategies.size(), activeStrategies.stream().map(LiveStrategy::getStrategyName).collect(Collectors.toList()));
        for (LiveStrategy strategy : activeStrategies) {
            try {
                startStrategy(strategy);
            } catch (Exception e) {
                log.error("Error starting strategy: {}", e.getMessage(), e);
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
        liveStrategyService.clearErrorMessage(strategy);

        LiveExecutor executor;
        try {
            executor = createExecutor(strategy);
        } catch (Exception e) {
            // If we cannot create the executor, we should notify the user and stop the strategy
            log.error("Error creating executor: {}", e.getMessage(), e);
            telegramNotifier.sendSysErrorNotification(String.format("Could not initialise Live Strategy '%s':", strategy.getStrategyName()), e, true);
            // Rethrow the exception to stop the strategy from starting
            throw e;
        }
        liveExecutorRepository.addStrategy(strategy.getStrategyName(), executor);

        executorService.submit(() -> {
            Thread.currentThread().setName("LiveStrategyExecutor-" + strategy.getStrategyName());
            MDC.put("strategyId", strategy.getStrategyName());
            MDC.put("instrument", executor.getDataManager().getInstrument().toString());
            try {
                executor.getDataManager().start();
            } catch (Exception e) {
                log.error("Error running strategy: {}", e.getMessage(), e);
                liveExecutorRepository.removeStrategy(strategy.getStrategyName());
                eventPublisher.publishErrorEvent(strategy.getStrategyName(), e);
                // Here we can notify the user that the strategy has stopped
                telegramNotifier.sendSysErrorNotification(String.format("Live Strategy %s has been stopped:", strategy.getStrategyName()), e, true);
            } finally {
                MDC.clear();
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
                    executor.getDataManager().stop("Strategy stopped via controller");
                } catch (Exception e) {
                    log.error("Error stopping strategy: {}", e.getMessage(), e);
                    log.warn("Attempting to force stop strategy: {}", strategyName);
                    executor.onStop(String.format("Error stopping via DataManager for strategy: %s", e.getMessage()));
                }
            }
        } catch (Exception e) {
            // We have made some assumptions in the system that strategies shouldn't fail to stop
            // If this happens, some logic will need to be refactored
            log.error("Error stopping strategy - THIS SHOULD NOT HAPPEN!: {}", e.getMessage(), e);
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

        BrokerAccount brokerConfig = liveStrategy.getBrokerAccount();
        log.info("Strategy {} using broker account: {} ({})", strategyId, brokerConfig.getBrokerName(), brokerConfig.getBrokerType());

        final BrokerClient brokerClient = brokerClientFactory.createBrokerClientFromBrokerConfig(brokerConfig);

        BarSeries barSeries = new DefaultBarSeries(5000);

        // Validates the strategy configuration against the parameters of the strategy class
        config.validate();

        // Create strategy instance
        Strategy strategyInstance = strategyFactory.createStrategy(config.getStrategyClass(), strategyId);
        LiveExternalDataProvider dataProvider = new LiveExternalDataProvider(brokerClient, config.getInstrumentData().getInstrument());

        // Preload data to ensure the live strategy 'starts' with enough data for all calculations (indicators) to be valid
        // TODO: This should be based on either indicators or period size (eg we dont need a week of data for 1m period)
        OandaDataClient prefetchDataClient = new OandaDataClient(new OandaBrokerClient(oandaClient, null)); // A single use client for fetching candles TODO: Improve this
        prefetchDataClient.fetchCandles(
                config.getInstrumentData().getInstrument(),
                ZonedDateTime.now().minus(config.getPeriod().getDuration().multipliedBy(5000)),
                ZonedDateTime.now(),
                config.getPeriod().getDuration(),
                new ClientCallback() {
                    @Override
                    public boolean onCandle(Bar bar) {
                        barSeries.addBar(bar);
                        return true;
                    }

                    @Override
                    public void onError(Exception exception) {
                        log.error("Error fetching preloaded candles", exception);
                        throw new RuntimeException("Error fetching preloaded candles", exception);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Preloaded {} candles", barSeries.getBarCount());
                    }
                }
        );

        // Here we need to 'hack' how we have generated the bars
        // The last bar is the 'currentBar' which teh datamanager maintains state for seperately
        // So we remove the last bar from the general series
        Bar currentBar = barSeries.getBars().removeLast();
        DefaultDataManager dataManager = new DefaultDataManager(strategyId, config.getInstrumentData().getInstrument(), dataProvider, config.getPeriod().getDuration(), barSeries, eventPublisher, telegramNotifier);
        // And initialise the datamanager with the current bar, so it can handle events for the current bar on tick
        dataManager.initialise(currentBar, currentBar.getOpenTime().plus(config.getPeriod().getDuration()));

        Map<String, String> runParams = config.getRunParams().stream()
                .collect(Collectors.toMap(LiveStrategyConfig.RunParameter::getName, LiveStrategyConfig.RunParameter::getValue));

        strategyInstance.setParameters(runParams);

        // Init with an empty account
        AccountManager accountManager = new DefaultAccountManager(0, 0, 0);
        TradeManager tradeManager = new LiveTradeManager(brokerClient);
        // Set, so we have the ability to stop the strategy in case of background processes
        tradeManager.setDataManager(dataManager);

        RiskManagementService riskManagementService = new LiveRiskManager(riskManagementServiceClient, brokerConfig.getAccountId());

        LiveStateManager liveStateManager = new LiveStateManager(brokerClient, accountManager, tradeManager, eventPublisher, strategyInstance, config.getInstrumentData().getInstrument(), liveStrategyService, telegramNotifier);

        strategyInstance.setNotificationChatId(liveStrategy.getTelegramChatId());

        StrategyNewsUtil strategyNewsUtil = new StrategyNewsUtil(forexFactoryClient, true);

        LiveExecutor executor = new LiveExecutor(
                strategyInstance,
                tradeManager,
                accountManager,
                dataManager,
                eventPublisher,
                liveStateManager,
                riskManagementService,
                telegramNotifier,
                liveStrategyService,
                strategyNewsUtil
        );

        executor.initialise();
        dataManager.addDataListener(executor);

        return executor;
    }

    public void manuallyCloseTrade(Long strategyId, String tradeId) throws Exception {
        log.info("Manually closing trade with id: {} for strategy: {}", tradeId, strategyId);
        LiveStrategy strategy = liveStrategyService.getActiveStrategy(strategyId).orElseThrow(
                () -> new ApiException(String.format("Live Strategy with id %s is not active or does not exist", strategyId), ErrorType.BAD_REQUEST)
        );

        LiveExecutor executor = liveExecutorRepository.getStrategy(strategy.getStrategyName());
        if (executor == null) {
            throw new ApiException(String.format("Could not find running executor with strategy name: %s", strategy.getStrategyName()), ErrorType.BAD_REQUEST);
        }

        Map<Integer, Trade> trades = executor.getTrades();

        Trade tradeToClose = trades.getOrDefault(Integer.parseInt(tradeId), null);
        if (tradeToClose == null) {
            throw new ApiException(String.format("Trade with id: %s could not be found", tradeId), ErrorType.BAD_REQUEST);
        }

        executor.closeTrade(tradeId);
    }
}