package dev.jwtly10.liveservice.service;

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
import dev.jwtly10.liveservice.repository.RunnerRepository;
import dev.jwtly10.liveservice.service.biz.LiveStrategyService;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.common.LiveExternalDataProvider;
import dev.jwtly10.marketdata.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final RunnerRepository runnerRepository;
    private final OandaClient oandaClient;

    private final LiveStrategyService liveStrategyService;

    public LiveStrategyManager(EventPublisher eventPublisher, RunnerRepository runnerRepository, OandaClient oandaClient, LiveStrategyService liveStrategyService) {
        this.runnerRepository = runnerRepository;
        this.eventPublisher = eventPublisher;
        this.oandaClient = oandaClient;
        this.liveStrategyService = liveStrategyService;
    }

    @Scheduled(fixedDelayString = "3600000") // One hour for now (TODO: Make this configurable, and refactor to NOT use a job in this way)
    public void processPendingOptimisationTasks() {
        log.debug("Running strategy live job");

        List<LiveStrategy> strategies = liveStrategyService.getActiveLiveStrategies();

        if (strategies.isEmpty()) {
            log.debug("No active strategies to run");
            return;
        } else {
            log.debug("Found {} active strategies to run", strategies.size());
        }

        strategies.forEach(strategy -> {
            if (runnerRepository.getStrategy(strategy.getStrategyName()) == null) {
                try {
                    start(strategy, strategy.getStrategyName());
                } catch (Exception e) {
                    log.error("Error starting strategy", e);
                }
            }
        });

    }

    public void start(LiveStrategy liveStrategy, String strategyName) throws Exception {
        StrategyFactory strategyFactory = new DefaultStrategyFactory();
        OandaBrokerClient client = new OandaBrokerClient(oandaClient, liveStrategy.getBrokerAccount().getAccountId());

        LiveStrategyConfig config = liveStrategy.getConfig();
        // TODO: Validate config against the current strategy version. Need to notify if this is invalid
        Strategy strategy = strategyFactory.createStrategy(config.getStrategyClass(), strategyName);

        LiveExternalDataProvider dataProvider = new LiveExternalDataProvider(client, config.getInstrumentData().getInstrument());
        BarSeries barSeries = new DefaultBarSeries(5000);

        List<DefaultBar> preCandles = client.fetchCandles(config.getInstrumentData().getInstrument(), ZonedDateTime.now().minusDays(4), ZonedDateTime.now(), config.getPeriod().getDuration());
        preCandles.forEach(barSeries::addBar);
        barSeries.getBars().forEach(bar -> eventPublisher.publishEvent(new BarEvent(strategyName, config.getInstrumentData().getInstrument(), bar)));

        DefaultDataManager dataManager = new DefaultDataManager(strategyName, config.getInstrumentData().getInstrument(), dataProvider, config.getPeriod().getDuration(), barSeries, eventPublisher);
        dataManager.initialise(barSeries.getLastBar(), barSeries.getLastBar().getOpenTime().plus(config.getPeriod().getDuration()));

        Map<String, String> runParams = config.getRunParams().stream()
                .collect(Collectors.toMap(LiveStrategyConfig.RunParameter::getName, LiveStrategyConfig.RunParameter::getValue));

        // Empty account for now
        AccountManager accountManager = new DefaultAccountManager(0, 0, 0);
        TradeManager tradeManager = new LiveTradeManager(client);

        BrokerClient brokerClient = new OandaBrokerClient(oandaClient, liveStrategy.getBrokerAccount().getAccountId());
        LiveStateManager liveStateManager = new LiveStateManager(brokerClient, accountManager, tradeManager, eventPublisher, strategyName, config.getInstrumentData().getInstrument());

        strategy.setParameters(runParams);

        LiveExecutor executor = new LiveExecutor(
                strategy,
                tradeManager,
                accountManager,
                dataManager,
                eventPublisher,
                liveStateManager
        );

        executor.initialise();
        dataManager.addDataListener(executor);

        executorService.submit(() -> {
            Thread.currentThread().setName("LiveStrategyExecutor-" + strategy.getStrategyId());
            try {
                dataManager.start();
            } catch (Exception e) {
                log.error("Error running strategy", e);
                runnerRepository.removeStrategy(strategy.getStrategyId());
                eventPublisher.publishErrorEvent(strategy.getStrategyId(), e);
            }
        });

        runnerRepository.addStrategy(strategy.getStrategyId(), executor);
    }

    // TODO: Review, how do we handle stops? Do we?
    public boolean stopStrategy(String id) {
        LiveExecutor executor = runnerRepository.getStrategy(id);
        if (executor != null) {
            executor.onStop();
            return true;
        }
        return false;
    }
}