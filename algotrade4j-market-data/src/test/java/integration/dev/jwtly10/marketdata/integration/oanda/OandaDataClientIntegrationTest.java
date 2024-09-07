package integration.dev.jwtly10.marketdata.integration.oanda;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.common.ClientCallback;
import dev.jwtly10.marketdata.oanda.OandaClient;
import dev.jwtly10.marketdata.oanda.OandaDataClient;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "OANDA_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OANDA_ACCOUNT_ID", matches = ".+")
class OandaDataClientIntegrationTest {

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
}