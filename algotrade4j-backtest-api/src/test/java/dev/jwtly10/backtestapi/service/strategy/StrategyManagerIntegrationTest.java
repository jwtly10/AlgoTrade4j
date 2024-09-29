package dev.jwtly10.backtestapi.service.strategy;

import dev.jwtly10.backtestapi.model.StrategyConfig;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.event.*;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Period;
import dev.jwtly10.core.model.Timeframe;
import dev.jwtly10.marketdata.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;

/**
 * This integration tests checks that a Strategy with given data, parameters and timeframe can be started and stopped
 * And that the strategy generates the expected events.
 * This test should be run whenever we make changes to the core library, to ensure that existing strategies wont suddenly return different results.
 */
@EnabledIfEnvironmentVariable(named = "OANDA_API_KEY", matches = ".+")
@Slf4j
class StrategyManagerIntegrationTest {

    @Mock
    private EventPublisher eventPublisher;

    private StrategyManager strategyManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String apiKey = System.getenv("OANDA_API_KEY");
        String baseUrl = "https://api-fxpractice.oanda.com";
        assertNotNull(apiKey, "OANDA_API_KEY environment variable must be set");

        OandaClient oandaClient = new OandaClient(baseUrl, apiKey);
        strategyManager = new StrategyManager(eventPublisher, oandaClient);
    }

    @Test
    @Timeout(30)
    void testBasicBacktestRunForNASOanda() throws InterruptedException {
        StrategyConfig config = new StrategyConfig();
        config.setStrategyClass("IntegrationTestStrategy");
        config.setInstrumentData(Instrument.NAS100USD.getInstrumentData());
        config.setPeriod(Period.M15);
        config.setSpread(10);
        config.setSpeed(DataSpeed.INSTANT);
        config.setInitialCash(10000);
        config.setTimeframe(new Timeframe(
                ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
                ZonedDateTime.of(2023, 8, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        ));
        config.setRunParams(
                List.of(
                        new StrategyConfig.RunParameter("stopLossTicks", "300"),
                        new StrategyConfig.RunParameter("riskRatio", "5"),
                        new StrategyConfig.RunParameter("riskPercentage", "1"),
                        new StrategyConfig.RunParameter("balanceToRisk", "10000"),
                        new StrategyConfig.RunParameter("atrLength", "14"),
                        new StrategyConfig.RunParameter("atrSensitivity", "0.6"),
                        new StrategyConfig.RunParameter("relativeSize", "2"),
                        new StrategyConfig.RunParameter("shortSMALength", "50"),
                        new StrategyConfig.RunParameter("longSMALength", "0"),
                        new StrategyConfig.RunParameter("tradeDirection", "ANY"),
                        new StrategyConfig.RunParameter("startTradeTime", "9"),
                        new StrategyConfig.RunParameter("endTradeTime", "20"),
                        new StrategyConfig.RunParameter("riskProfile", "NONE")
                )
        );

        List<BaseEvent> capturedEvents = runStrategy(config);

        // Validate basic events
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof BarEvent), "No BarEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof TradeEvent), "No TradeEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof AccountEvent), "No AccountEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof StrategyStopEvent), "Strategy did not stop");

        // Validate trade profits
        List<TradeEvent> tradeEvents = capturedEvents.stream()
                .filter(e -> e instanceof TradeEvent && ((((TradeEvent) e).getAction() == TradeEvent.Action.CLOSE)))
                .map(e -> (TradeEvent) e)
                .toList();
        log.info("Total Trades: {}", tradeEvents.size());

        for (int i = 0; i < Math.min(100, tradeEvents.size()); i++) {
            double profit = tradeEvents.get(i).getTrade().getProfit();
            if (profit > 0) {
                assertTrue(profit < 550 && profit > 490, "Given a 5 RR with 1% risk. Profit should be between 490 and 550 (spread may cause discrepancy). Profit was: " + profit);
            } else {
                assertTrue(profit < -90 && profit > -150, "Given a 5 RR with 1% risk. Loss should be between -90 and -150 (spread may cause discrepancy). Loss was: " + profit);
            }
        }

        // Validate account results
        AccountEvent accountEvent = (AccountEvent) capturedEvents.stream()
                .filter(e -> e instanceof AccountEvent)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("No AccountEvent was published"));

        double balance = accountEvent.getAccount().getBalance();
        double equity = accountEvent.getAccount().getEquity();
        double profit = balance - accountEvent.getAccount().getInitialBalance();
        double profitPercentage = profit / accountEvent.getAccount().getInitialBalance() * 100;

        log.info("""
                        ACCOUNT RESULTS:
                        Account Balance: {}
                        Account Equity: {}
                        Profit: {}
                        Profit %: {}
                        """,
                balance,
                equity,
                profit,
                profitPercentage
        );

        double epsilon = 0.01;
        assertEquals(28586.58, balance, epsilon, "Balance should be ~= 28586.58");
        assertEquals(28586.58, equity, epsilon, "Equity should be ~= 28586.58");
        assertEquals(18586.58, profit, epsilon, "Profit should be ~= 18586.58");
        assertEquals(185.86, profitPercentage, epsilon, "Profit percentage should be ~= 185.86%");
    }

    @Test
//    @Timeout(30)
    void testRiskManagementForMFF() throws InterruptedException {
        StrategyConfig config = new StrategyConfig();
        config.setStrategyClass("IntegrationTestStrategy");
        config.setInstrumentData(Instrument.NAS100USD.getInstrumentData());
        config.setPeriod(Period.M15);
        config.setSpread(10);
        config.setSpeed(DataSpeed.INSTANT);
        config.setInitialCash(10000);
        config.setTimeframe(new Timeframe(
                ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
                ZonedDateTime.of(2023, 1, 10, 0, 0, 0, 0, ZoneId.of("UTC"))
        ));
        config.setRunParams(
                List.of(
                        new StrategyConfig.RunParameter("stopLossTicks", "300"),
                        new StrategyConfig.RunParameter("riskRatio", "5"),
                        new StrategyConfig.RunParameter("riskPercentage", "1"),
                        new StrategyConfig.RunParameter("balanceToRisk", "10000"),
                        new StrategyConfig.RunParameter("atrLength", "14"),
                        new StrategyConfig.RunParameter("atrSensitivity", "0.6"),
                        new StrategyConfig.RunParameter("relativeSize", "2"),
                        new StrategyConfig.RunParameter("shortSMALength", "50"),
                        new StrategyConfig.RunParameter("longSMALength", "0"),
                        new StrategyConfig.RunParameter("tradeDirection", "ANY"),
                        new StrategyConfig.RunParameter("startTradeTime", "9"),
                        new StrategyConfig.RunParameter("endTradeTime", "20"),
                        new StrategyConfig.RunParameter("riskProfile", "MFF")
                )
        );

        List<BaseEvent> capturedEvents = runStrategy(config);

        // Validate basic events
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof BarEvent), "No BarEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof TradeEvent), "No TradeEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof AccountEvent), "No AccountEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof StrategyStopEvent), "Strategy did not stop");

        // Validate trade profits
        List<TradeEvent> tradeEvents = capturedEvents.stream()
                .filter(e -> e instanceof TradeEvent && ((((TradeEvent) e).getAction() == TradeEvent.Action.CLOSE)))
                .map(e -> (TradeEvent) e)
                .toList();
        log.info("Total Trades: {}", tradeEvents.size());

        tradeEvents.forEach(tradeEvent -> {
            log.info("Trade: {}", tradeEvent.getTrade());
        });
    }

    private List<BaseEvent> runStrategy(StrategyConfig config) throws InterruptedException {
        String strategyId = "int-test-strategy-id";
        CountDownLatch latch = new CountDownLatch(1);

        ArgumentCaptor<BaseEvent> eventCaptor = ArgumentCaptor.forClass(BaseEvent.class);

        doAnswer(invocation -> {
            BaseEvent event = invocation.getArgument(0);
            if (event instanceof StrategyStopEvent && ((StrategyStopEvent) event).getStrategyId().equals(strategyId)) {
                latch.countDown();
            }
            return null;
        }).when(eventPublisher).publishEvent(eventCaptor.capture());

        strategyManager.startStrategy(config, strategyId);

        assertTrue(latch.await(700, TimeUnit.SECONDS), "Strategy did not complete in time");
//        assertTrue(latch.await(30, TimeUnit.SECONDS), "Strategy did not complete in time");

        List<BaseEvent> capturedEvents = eventCaptor.getAllValues();
        assertFalse(capturedEvents.isEmpty(), "No events were published");

        return capturedEvents;
    }
}