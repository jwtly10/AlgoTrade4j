package integration.dev.jwtly10.marketdata.integration.oanda;

import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.oanda.OandaClient;
import dev.jwtly10.marketdata.oanda.response.OandaPriceResponse;
import dev.jwtly10.marketdata.oanda.utils.OandaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "OANDA_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OANDA_ACCOUNT_ID", matches = ".+")
public class OandaStreamingClientTest {
    private OandaClient client;

    @BeforeEach
    void setUp() {
        String apiKey = System.getenv("OANDA_API_KEY");
        String accountId = System.getenv("OANDA_ACCOUNT_ID");
        String baseUrl = "https://api-fxpractice.oanda.com";

        assertNotNull(apiKey, "OANDA_API_KEY environment variable must be set");
        assertNotNull(accountId, "OANDA_ACCOUNT_ID environment variable must be set");

        client = new OandaClient(baseUrl, apiKey, accountId);
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
}