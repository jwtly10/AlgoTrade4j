package dev.jwtly10.marketdata.news.forexfactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ForexFactoryClientTest {
    @Test
    void getMockedNews() {
        // Given
        ForexFactoryClient forexFactoryClient = new ForexFactoryClient();

        // When
        try {
            var res = forexFactoryClient.getMockedNews();
            assertEquals(98, res.size());
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }

}