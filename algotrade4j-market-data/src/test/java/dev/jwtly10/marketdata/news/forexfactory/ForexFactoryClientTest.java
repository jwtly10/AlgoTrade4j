package dev.jwtly10.marketdata.news.forexfactory;

import org.junit.jupiter.api.Test;

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

}