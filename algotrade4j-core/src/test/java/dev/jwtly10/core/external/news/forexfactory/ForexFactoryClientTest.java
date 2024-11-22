package dev.jwtly10.core.external.news.forexfactory;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ForexFactoryClientTest {
    @Test
    void testCanParseForexFactoryJson() {
        ForexFactoryClient forexFactoryClient = new ForexFactoryClient();


        List<ForexFactoryNews> res = new ArrayList<>();
        try {
            res = forexFactoryClient.getMockedNews();
            assertEquals(99, res.size());
        } catch (Exception e) {
            fail("Should not throw exception");
        }

        // Ensuring we properly parse the data format from the API (specifically dates)
        // This is the first item in the json
//        {
//            "title": "Daylight Saving Time Shift",
//                "country": "CAD",
//                "date": "2024-11-03T03:00:00-05:00", (08:00 in UTC)
//                "impact": "Holiday",
//                "forecast": "",
//                "previous": ""
//        },
        assertEquals("Daylight Saving Time Shift", res.getFirst().title());
        assertEquals("CAD", res.getFirst().country());
        // toInstant() so we can properly compare the values
        assertEquals(ZonedDateTime.of(2024, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC).toInstant(), res.getFirst().date().toInstant());
        assertEquals(ForexFactoryNews.Impact.HOLIDAY, res.getFirst().impact());
        assertEquals("", res.getFirst().forecast());
        assertEquals("", res.getFirst().previous());
    }

    @Test
    void getADaysUSDNews() throws IOException, URISyntaxException {
        ForexFactorySearchParams searchParams = ForexFactorySearchParams.builder()
                .country("USD")
                .date(LocalDate.of(2024, 11, 5))
                .impact(ForexFactoryNews.Impact.LOW)
                .build();


        var client = new ForexFactoryClient();

        var res = client.searchMockedNews(searchParams);

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

        assertEquals(2, res.size(), "There should have been only 2 news events on 2024-11-05");
    }
}