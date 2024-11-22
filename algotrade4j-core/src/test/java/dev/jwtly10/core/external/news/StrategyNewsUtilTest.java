package dev.jwtly10.core.external.news;

import dev.jwtly10.core.external.news.forexfactory.ForexFactoryNews;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StrategyNewsUtilTest {
    private final StrategyNewsUtil util = new StrategyNewsUtil();
    // This is some mock data that we are using for these unit tests
    // If the current time was 2024-11-05T15:11:00, we should see the presidential election as the next high impact event, and the ISM Service PMI as the last high impact event
//    {
//        "title": "ISM Services PMI",
//            "country": "USD",
//            "date": "2024-11-05T10:00:00-05:00",
//            "impact": "High",
//            "forecast": "53.8",
//            "previous": "54.9"
//    },
//    {
//        "title": "ECB President Lagarde Speaks",
//            "country": "EUR",
//            "date": "2024-11-05T10:10:00-05:00",
//            "impact": "Medium",
//            "forecast": "",
//            "previous": ""
//    },
//    {
//        "title": "Presidential Election",
//            "country": "USD",
//            "date": "2024-11-05T10:15:00-05:00",
//            "impact": "High",
//            "forecast": "",
//            "previous": ""
//    },

    @Test
    void getNextHighImpactEvent() throws IOException {
        // 2024-11-05T15:11:00[UTC] Should return the presidential election as the next event
        ZonedDateTime mockNow = ZonedDateTime.parse("2024-11-05T15:11:00Z");
        ForexFactoryNews nextHighImpactEvent = util.getNextHighImpactEvent("USD", mockNow).orElseThrow(
                () -> new IllegalStateException("Expected a high impact event")
        );

        assertNotNull(nextHighImpactEvent);

        assertEquals("Presidential Election", nextHighImpactEvent.title());
        assertEquals("USD", nextHighImpactEvent.country());
        assertEquals("High", nextHighImpactEvent.impact().getValue());
    }

    @Test
    void getLastHighImpactEvent() throws IOException {
        // 2024-11-05T15:11:00[UTC] Should return the presidential election as the next event
        ZonedDateTime mockNow = ZonedDateTime.parse("2024-11-05T15:11:00Z");
        ForexFactoryNews nextHighImpactEvent = util.getLastHighImpactEvent("USD", mockNow).orElseThrow(
                () -> new IllegalStateException("Expected a high impact event")
        );

        assertNotNull(nextHighImpactEvent);

        assertEquals("ISM Services PMI", nextHighImpactEvent.title());
        assertEquals("USD", nextHighImpactEvent.country());
        assertEquals("High", nextHighImpactEvent.impact().getValue());
    }
}