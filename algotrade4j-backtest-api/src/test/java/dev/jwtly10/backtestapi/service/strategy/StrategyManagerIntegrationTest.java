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
    void testBacktestRunForNASOanda() throws InterruptedException {
        StrategyConfig config = new StrategyConfig();
        config.setStrategyClass("PinnedDJATRStrategy");
        config.setInstrumentData(Instrument.NAS100USD.getInstrumentData());
        config.setPeriod(Period.M15);
        config.setSpread(10);
        config.setSpeed(DataSpeed.INSTANT);
        config.setInitialCash(10000);
        config.setTimeframe(new Timeframe(
                ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")), // 2023-01-01 00:00:00 UTC
                ZonedDateTime.of(2023, 8, 1, 0, 0, 0, 0, ZoneId.of("UTC")) // 2023-08-01 00:00:00 UTC
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
                        new StrategyConfig.RunParameter("endTradeTime", "20")
                )
        );

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

        // Wait for the strategy to stop
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Strategy did not complete in time");

        List<BaseEvent> capturedEvents = eventCaptor.getAllValues();
        assertFalse(capturedEvents.isEmpty(), "No events were published");

        // Validate
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof BarEvent), "No BarEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof TradeEvent), "No TradeEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof AccountEvent), "No AccountEvent was published");
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof StrategyStopEvent), "Strategy did not stop");

        // Validate some of the profit of the trades
        List<TradeEvent> tradeEvents = capturedEvents.stream()
                .filter(e -> e instanceof TradeEvent && ((((TradeEvent) e).getAction() == TradeEvent.Action.CLOSE)))
                .map(e -> (TradeEvent) e)
                .toList();
        log.info("Total Trades: {}", tradeEvents.size());

        // Checking the first 100 trades are as expected
        for (int i = 0; i < 100; i++) {
            double profit = tradeEvents.get(i).getTrade().getProfit();
            // Is profitable trade
            // NB: If this fails due to profit outside of range, it may be due to SlippageModel.java, which is a crude slippage model for roughly realistic slippage calculations
            // If it is changed, it may affect this test.
            if (profit > 0) {
                assertTrue(profit < 550 && profit > 490, "Given a 5 RR with 1% risk. Profit should be between 490 and 550 (spread may cause discrepancy). Profit was: " + profit);
            } else {
                assertTrue(profit < -90 && profit > -150, "Given a 5 RR with 1% risk. Loss should be between -90 and -150 (spread may cause discrepancy). Loss was: " + profit);
            }
        }

        // Validate output of the strategy
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

        // Assertions to check if numbers are roughly equal
        double epsilon = 0.01; // Tolerance

        assert Math.abs(balance - 28586.58) < epsilon : "Balance should be ~= 27119.16";
        assert Math.abs(equity - 28586.58) < epsilon : "Equity should be ~= 27119.16";
        assert Math.abs(profit - 18586.58) < epsilon : "Profit should be ~= 17119.16";
        assert Math.abs(profitPercentage - 185.86) < epsilon : "Profit percentage should be ~= 171.19%";
    }
}