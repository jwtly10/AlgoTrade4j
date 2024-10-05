package integration.dev.jwtly10.marketdata.news.forexfactory;

import dev.jwtly10.marketdata.news.forexfactory.ForexFactoryClient;
import dev.jwtly10.marketdata.news.forexfactory.ForexFactoryNews;
import dev.jwtly10.marketdata.news.forexfactory.ForexFactorySearchParams;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void getADaysUSDNews() throws IOException {
        ForexFactorySearchParams searchParams = ForexFactorySearchParams.builder()
                .country("USD")
                .date(LocalDate.of(2024, 10, 4))
                .impact(ForexFactoryNews.Impact.LOW)
                .build();


        var client = new ForexFactoryClient();

        var res = client.searchNews(searchParams);

        res.forEach(newsItem -> {
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
                            newsItem.title(),
                            newsItem.country(),
                            newsItem.date(),
                            newsItem.impact().getValue(),
                            newsItem.forecast(),
                            newsItem.previous());
                }
        );

        assertEquals(res.size(), 4, "There should have been only one news event today.");
    }

}