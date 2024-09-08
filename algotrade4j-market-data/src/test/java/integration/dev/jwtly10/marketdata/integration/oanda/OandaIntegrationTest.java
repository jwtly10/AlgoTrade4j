package integration.dev.jwtly10.marketdata.integration.oanda;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.common.ClientCallback;
import dev.jwtly10.marketdata.oanda.OandaClient;
import dev.jwtly10.marketdata.oanda.OandaDataClient;
import dev.jwtly10.marketdata.oanda.models.TradeStateFilter;
import dev.jwtly10.marketdata.oanda.response.OandaPriceResponse;
import dev.jwtly10.marketdata.oanda.utils.OandaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A collection of integration tests for the Oanda API client.
 */
@EnabledIfEnvironmentVariable(named = "OANDA_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OANDA_ACCOUNT_ID", matches = ".+")
class OandaIntegrationTest {

    private OandaClient client;
    private OandaDataClient oandaClient;

    @BeforeEach
    void setUp() {
        String apiKey = System.getenv("OANDA_API_KEY");
        String accountId = System.getenv("OANDA_ACCOUNT_ID");
        String baseUrl = "https://api-fxpractice.oanda.com";

        assertNotNull(apiKey, "OANDA_API_KEY environment variable must be set");
        assertNotNull(accountId, "OANDA_ACCOUNT_ID environment variable must be set");

        client = new OandaClient(baseUrl, apiKey, accountId);
        oandaClient = new OandaDataClient(client);
    }

    @Test
    @Timeout(value = 30)
    void testFetchCandlesSuccess() throws InterruptedException {
        Instrument instrument = Instrument.NAS100USD;
        ZonedDateTime from = ZonedDateTime.now().minusDays(7);
        ZonedDateTime to = ZonedDateTime.now();
        Duration period = Duration.ofHours(1);

        List<Bar> receivedBars = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        oandaClient.fetchCandles(instrument, from, to, period, new ClientCallback() {
            @Override
            public boolean onCandle(Bar bar) {
                receivedBars.add(bar);
                return true;
            }

            @Override
            public void onError(Exception e) {
                fail("Data client threw an error when it shouldn't have: ", e);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        boolean completed = latch.await(25, TimeUnit.SECONDS);
        assertTrue(completed, "Fetch operation timed out");

        assertFalse(receivedBars.isEmpty(), "No bars were received");
        assertTrue(receivedBars.size() > 100, "Expected more than 100 bars for a 7-day period");

        for (Bar bar : receivedBars) {
            assertEquals(instrument, bar.getInstrument(), "Bar instrument should match requested instrument");
            assertEquals(period, bar.getTimePeriod(), "Bar period should match requested period");
            assertTrue(bar.getOpenTime().isAfter(from) || bar.getOpenTime().equals(from), "Bar open time should be after or equal to 'from' time");
            assertTrue(bar.getOpenTime().isBefore(to) || bar.getOpenTime().equals(to), "Bar open time should be before or equal to 'to' time");
            assertNotNull(bar.getOpen(), "Open price should not be null");
            assertNotNull(bar.getHigh(), "High price should not be null");
            assertNotNull(bar.getLow(), "Low price should not be null");
            assertNotNull(bar.getClose(), "Close price should not be null");
            assertNotNull(bar.getVolume(), "Volume should not be null");
        }
    }

    @Test
    @Timeout(value = 30)
    void testFetchCandlesExceedsLimit() throws InterruptedException {
        Instrument instrument = Instrument.NAS100USD;
        ZonedDateTime from = ZonedDateTime.now().minusYears(1);
        ZonedDateTime to = ZonedDateTime.now();
        Duration period = Duration.ofMinutes(15);

        List<Bar> receivedBars = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        oandaClient.fetchCandles(instrument, from, to, period, new ClientCallback() {
            @Override
            public boolean onCandle(Bar bar) {
                receivedBars.add(bar);
                return true;
            }

            @Override
            public void onError(Exception e) {
                fail("Data client threw an error when it shouldn't have: ", e);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        boolean completed = latch.await(25, TimeUnit.SECONDS);
        assertTrue(completed, "Fetch operation timed out");

        assertFalse(receivedBars.isEmpty(), "No bars were received");
        assertTrue(receivedBars.size() > 10000, "Expected more than 10000 bars for a 1-year period");

        for (Bar bar : receivedBars) {
            assertEquals(instrument, bar.getInstrument(), "Bar instrument should match requested instrument");
            assertEquals(period, bar.getTimePeriod(), "Bar period should match requested period");
            assertTrue(bar.getOpenTime().isAfter(from) || bar.getOpenTime().equals(from), "Bar open time should be after or equal to 'from' time");
            assertTrue(bar.getOpenTime().isBefore(to) || bar.getOpenTime().equals(to), "Bar open time should be before or equal to 'to' time");
            assertNotNull(bar.getOpen(), "Open price should not be null");
            assertNotNull(bar.getHigh(), "High price should not be null");
            assertNotNull(bar.getLow(), "Low price should not be null");
            assertNotNull(bar.getClose(), "Close price should not be null");
            assertNotNull(bar.getVolume(), "Volume should not be null");
        }
    }

    @Test
    @Timeout(value = 30)
    void testFetchCandlesInvalidDate() throws InterruptedException {
        Instrument instrument = Instrument.NAS100USD;
        ZonedDateTime from = ZonedDateTime.now().minusYears(1);
        ZonedDateTime to = ZonedDateTime.now().plusDays(10);
        Duration period = Duration.ofMinutes(15);

        List<Bar> receivedBars = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> capturedError = new AtomicReference<>();

        oandaClient.fetchCandles(instrument, from, to, period, new ClientCallback() {
            @Override
            public boolean onCandle(Bar bar) {
                receivedBars.add(bar);
                return true;
            }

            @Override
            public void onError(Exception e) {
                capturedError.set(e);
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Callback did not complete in time");

        assertNotNull(capturedError.get(), "Expected an error, but none was received");

        Exception error = capturedError.get();
        assertEquals("Invalid to date - cannot be in the future",
                error.getMessage());

        assertTrue(receivedBars.isEmpty(), "Expected no bars to be received");
    }

    @Test
    void testCanStreamPriceData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger receivedTicks = new AtomicInteger(0);

        client.streamPrices(List.of(Instrument.NAS100USD), new OandaClient.PriceStreamCallback() {
            @Override
            public void onPrice(OandaPriceResponse price) {
                System.out.println(price);
                DefaultTick tick = OandaUtils.mapPriceToTick(price);
                System.out.println(tick);
                receivedTicks.incrementAndGet();
                if (receivedTicks.get() >= 5) {
                    latch.countDown();
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onComplete() {
                System.out.println("Stream complete");
                latch.countDown();
            }
        });

        boolean completed = latch.await(3, TimeUnit.SECONDS);

        if (!completed) {
            System.out.println("Test timed out. Received " + receivedTicks.get() + " ticks.");
        }

        assert receivedTicks.get() > 0 : "No ticks were received";
    }

    @Test
    void testCanFetchTrades() throws Exception {
        // Test can fetch with instrument filter
        var res = client.fetchTrades(null, TradeStateFilter.ALL, Instrument.NAS100USD, 10);
        System.out.println(res);
        assertNotNull(res.lastTransactionID());
//        assertNotEquals(0, res.trades().size());

        // Test can fetch with no instrument filter
        var res2 = client.fetchTrades(null, TradeStateFilter.ALL, null, 10);
        System.out.println(res2);
        assertNotNull(res2.lastTransactionID());
//        assertNotEquals(0, res2.trades().size());

        // Fetch trades with specific ids
        var res3 = client.fetchTrades(List.of("2", "3"), TradeStateFilter.ALL, null, 10);
        System.out.println(res3);
        assertNotNull(res3.lastTransactionID());
//        assertEquals(2, res3.trades().size());

        // Fetch open trades
        var res4 = client.fetchTrades(null, TradeStateFilter.OPEN, null, 10);
        System.out.println(res4);
        assertNotNull(res4.lastTransactionID());
//        assertNotEquals(0, res4.trades().size());
    }
}