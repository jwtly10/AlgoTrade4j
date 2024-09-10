package dev.jwtly10.liveservice.service;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.data.DataListener;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.*;
import dev.jwtly10.core.event.async.AsyncIndicatorsEvent;
import dev.jwtly10.core.execution.DefaultTradeManager;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.DefaultStrategyFactory;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.strategy.StrategyFactory;
import dev.jwtly10.liveservice.executor.LiveExecutor;
import dev.jwtly10.liveservice.executor.LiveStateManager;
import dev.jwtly10.liveservice.model.LiveStrategyConfig;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.common.LiveExternalDataProvider;
import dev.jwtly10.marketdata.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.WebSocketSession;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class LiveStrategyManager {
    private final ConcurrentHashMap<String, DataListener> runningStrategies = new ConcurrentHashMap<>();

    private final LiveStrategyWSHandler webSocketHandler;

    private final EventPublisher eventPublisher;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Value("${oanda.api.key}")
    private String oandaApiKey;
    @Value("${oanda.account.id}")
    private String oandaAccountId;
    @Value("${oanda.api.url}")
    private String oandaApiUrl;

    public LiveStrategyManager(LiveStrategyWSHandler webSocketHandler, EventPublisher eventPublisher) {
        this.webSocketHandler = webSocketHandler;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "600000")
    public void processPendingOptimisationTasks() {
        log.debug("Running strategy live job");

        LiveStrategyConfig config = LiveStrategyConfig.builder()
                .strategyClass("DJATRStrategy")
                .initialCash(100000)
                .instrumentData(Instrument.NAS100USD.getInstrumentData())
                .period(Period.M15)
                .speed(DataSpeed.INSTANT)
                .spread(1)
                .timeframe(null)
                .runParams(
                        List.of(
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("Trade Direction")
                                        .value("LONG")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("Start Trading Time (Hour)")
                                        .value("9")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("End Trading Time (Hour)")
                                        .value("20")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("Stop Loss Size (pips)")
                                        .value("30")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("Risk Ratio (RR)")
                                        .value("5")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("Risk % Per trade")
                                        .value("1.0")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("ATR Length")
                                        .value("14")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("ATR Sensitivity")
                                        .value("0.6")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("Relative Size Diff")
                                        .value("2.0")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("Short EMA Length")
                                        .value("50")
                                        .build(),
                                LiveStrategyConfig.RunParameter.builder()
                                        .name("Long EMA Length")
                                        .value("0")
                                        .build()
                        )
                )
                .build();

        // We've created the config. Now we create a websocket listener for this event
        WebSocketSession session = null;
        WebSocketEventListener listener = null;
        int maxAttempts = 25; // 5 seconds total (25 * 200ms)
        int attempts = 0;

        while (attempts < maxAttempts) {
            session = webSocketHandler.getSessionForStrategy("testing");
            if (session != null) {
                listener = webSocketHandler.getListenerForSession(session);
                if (listener != null) {
                    break;
                }
            }

            attempts++;
            try {
                Thread.sleep(200); // Wait for 200ms before retrying
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for session/listener");
            }
        }

        listener.subscribe(BarEvent.class);
        listener.subscribe(TradeEvent.class);
        listener.subscribe(IndicatorEvent.class);
        listener.subscribe(StrategyStopEvent.class);
        listener.subscribe(AccountEvent.class);
        listener.subscribe(AnalysisEvent.class);
        listener.subscribe(LogEvent.class);
        listener.subscribe(ErrorEvent.class);
        listener.subscribe(AsyncIndicatorsEvent.class);

        try {
            start(config, "testing");
        } catch (Exception e) {
            log.error("Error starting strategy", e);
        }
    }

    public void start(LiveStrategyConfig config, String strategyName) throws Exception {
//        config.validate();

        StrategyFactory strategyFactory = new DefaultStrategyFactory();
        OandaClient oandaClient = new OandaClient(oandaApiUrl, oandaApiKey, oandaAccountId);
        OandaBrokerClient client = new OandaBrokerClient(oandaClient);

        // TODO: Validate config against the current strategy version. Need to notify if this is invalid
        Strategy strategy = strategyFactory.createStrategy(config.getStrategyClass(), strategyName);

        LiveExternalDataProvider dataProvider = new LiveExternalDataProvider(oandaClient, config.getInstrumentData().getInstrument());
        BarSeries barSeries = new DefaultBarSeries(5000);

        // We should prefil the bar series with some historic data
        List<DefaultBar> preCandles = client.fetchCandles(config.getInstrumentData().getInstrument(), ZonedDateTime.now().minusDays(2), ZonedDateTime.now(), config.getPeriod().getDuration());
        preCandles.forEach(barSeries::addBar);
        barSeries.getBars().forEach(bar -> eventPublisher.publishEvent(new BarEvent(strategyName, config.getInstrumentData().getInstrument(), bar)));

        DefaultDataManager dataManager = new DefaultDataManager(strategyName, config.getInstrumentData().getInstrument(), dataProvider, config.getPeriod().getDuration(), barSeries, eventPublisher);
        dataManager.initialise(barSeries.getLastBar(), barSeries.getLastBar().getOpenTime().plus(config.getPeriod().getDuration()));

        Map<String, String> runParams = config.getRunParams().stream()
                .collect(Collectors.toMap(LiveStrategyConfig.RunParameter::getName, LiveStrategyConfig.RunParameter::getValue));

        Tick currentTick = new DefaultTick();

        // Empty account for now
        AccountManager accountManager = new DefaultAccountManager(0, 0, 0);
        TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, strategyName, eventPublisher);

        BrokerClient brokerClient = new OandaBrokerClient(oandaClient);
        LiveStateManager liveStateManager = new LiveStateManager(brokerClient, accountManager, tradeManager, eventPublisher, strategyName);

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
                runningStrategies.remove(strategy.getStrategyId());
                eventPublisher.publishErrorEvent(strategy.getStrategyId(), e);
            }
        });

        runningStrategies.put(strategy.getStrategyId(), executor);
    }

    public boolean stopStrategy(String id) {
        return true;
    }
}