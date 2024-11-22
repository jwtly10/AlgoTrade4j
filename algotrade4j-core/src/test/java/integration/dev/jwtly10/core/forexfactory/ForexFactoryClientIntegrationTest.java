package integration.dev.jwtly10.core.forexfactory;

import dev.jwtly10.core.external.news.forexfactory.ForexFactoryClient;
import dev.jwtly10.core.external.news.forexfactory.ForexFactoryNews;
import dev.jwtly10.core.external.news.forexfactory.ForexFactorySearchParams;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ForexFactoryClientIntegrationTest {

    @Test
    void testGetNewsWithNoFilter() throws IOException {
        ForexFactoryClient client = new ForexFactoryClient();

        List<ForexFactoryNews> news = client.searchNews(ForexFactorySearchParams.builder().build());

        System.out.println("10 News data from this week");
        for (int i = 0; i < 10; i++) {
            ForexFactoryNews n = news.get(i);
            System.out.printf("""
                            ================================
                            Title: %s,
                            Country: %s,
                            Date: %s,
                            Impact: %s,
                            Forecast: %s,
                            Previous: %s
                            ================================
                            """,
                    n.title(),
                    n.country(),
                    n.date(),
                    n.impact().getValue(),
                    n.forecast(),
                    n.previous()
            );
        }

        assertFalse(news.isEmpty(), "ForexFactory news should not be empty");
    }

}