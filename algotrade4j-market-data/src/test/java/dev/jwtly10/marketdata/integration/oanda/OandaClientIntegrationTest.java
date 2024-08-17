package dev.jwtly10.marketdata.integration.oanda;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.instruments.OandaInstrument;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.marketdata.oanda.OandaClient;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "OANDA_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OANDA_ACCOUNT_ID", matches = ".+")
class OandaClientIntegrationTest {

    private OandaClient oandaClient;

    @BeforeEach
    void setUp() {
        String apiKey = System.getenv("OANDA_API_KEY");
        String accountId = System.getenv("OANDA_ACCOUNT_ID");
        String baseUrl = "https://api-fxpractice.oanda.com";

        assertNotNull(apiKey, "OANDA_API_KEY environment variable must be set");
        assertNotNull(accountId, "OANDA_ACCOUNT_ID environment variable must be set");

        OkHttpClient httpClient = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        oandaClient = new OandaClient(apiKey, accountId, baseUrl, httpClient, objectMapper);
    }

    @Test
    void testFetchBarsIntegration() {
        ZoneId newYorkZone = ZoneId.of("America/New_York");
        ZonedDateTime to = ZonedDateTime.now(newYorkZone);
        ZonedDateTime from = to.minusDays(1);
        Duration period = Duration.ofMinutes(5);

        List<DefaultBar> bars = oandaClient.fetchBars(OandaInstrument.EUR_USD, from, to, period);

        assertFalse(bars.isEmpty(), "Should fetch some bars");
        assertTrue(bars.size() > 100, "Should fetch more than 100 bars for a day with 5-minute intervals");

        DefaultBar firstBar = bars.getFirst();
        assertNotNull(firstBar.getOpenTime());
        assertTrue(firstBar.getOpenTime().isAfter(from) || firstBar.getOpenTime().equals(from));
        assertTrue(firstBar.getOpenTime().isBefore(to));
        assertEquals(period, firstBar.getTimePeriod());
        assertEquals(OandaInstrument.EUR_USD.toString(), firstBar.getSymbol());

        assertNotNull(firstBar.getOpen());
        assertNotNull(firstBar.getHigh());
        assertNotNull(firstBar.getLow());
        assertNotNull(firstBar.getClose());
        assertNotNull(firstBar.getVolume());
    }

    @Test
    void testFetchBarsWithDifferentInstrumentAndPeriod() {

        ZoneId newYorkZone = ZoneId.of("America/New_York");
        ZonedDateTime to = ZonedDateTime.now(newYorkZone);
        ZonedDateTime from = to.minusHours(4);
        Duration period = Duration.ofMinutes(5);


        List<DefaultBar> bars = oandaClient.fetchBars(OandaInstrument.USD_JPY, from, to, period);

        assertFalse(bars.isEmpty(), "Should fetch some bars");
        System.out.println("bars.size() = " + bars.size());

        for (DefaultBar bar : bars) {
            System.out.println("bar = " + bar);
        }
        assertTrue(bars.size() > 10, "Should fetch more than 10 bars for 4 hours with 15-minute intervals");

        DefaultBar firstBar = bars.getFirst();
        assertEquals(period, firstBar.getTimePeriod());
        assertEquals(OandaInstrument.USD_JPY.toString(), firstBar.getSymbol());
    }

    @Test
    void testFetchBarsWithNoDataInRange() {
        ZonedDateTime to = ZonedDateTime.now().minusYears(10);
        ZonedDateTime from = to.minusDays(1);
        Duration period = Duration.ofMinutes(5);

        List<DefaultBar> bars = oandaClient.fetchBars(OandaInstrument.EUR_USD, from, to, period);

        assertTrue(bars.isEmpty(), "Should return empty list for invalid date range");
    }

    @Test
    void testFetchBarsWithInvalidDate() {
        ZonedDateTime to = ZonedDateTime.now().plusDays(10);
        ZonedDateTime from = ZonedDateTime.now();
        Duration period = Duration.ofMinutes(5);

        List<DefaultBar> bars = oandaClient.fetchBars(OandaInstrument.EUR_USD, from, to, period);

        assertTrue(bars.isEmpty(), "Should return empty list for invalid date range");
    }
}