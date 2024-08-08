package dev.jwtly10.core.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BaseEventTest {

    @Test
    void testConstructor() {
        String strategyId = "testStrategy";
        String type = "TEST";
        String symbol = "AAPL";

        TestEvent event = new TestEvent(strategyId, type, symbol);

        assertNotNull(event.getEventId());
        UUID.fromString(event.getEventId());
        assertTrue(true);
        assertEquals(strategyId, event.getStrategyId());
        assertEquals(type, event.getType());
        assertEquals(symbol, event.getSymbol());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testTimestampIsCurrentTime() {
        TestEvent event = new TestEvent("testStrategy", "TEST", "AAPL");
        ZonedDateTime now = ZonedDateTime.now();

        // Allow for a small time difference (e.g., 1 second) due to execution time
        assertTrue(Math.abs(event.getTimestamp().toEpochSecond() - now.toEpochSecond()) <= 1);
    }

    @Test
    void testToJson() throws JsonProcessingException {
        String strategyId = "testStrategy";
        String type = "TEST";
        String symbol = "AAPL";

        TestEvent event = new TestEvent(strategyId, type, symbol);
        String json = event.toJson();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);

        assertEquals(event.getEventId(), jsonNode.get("eventId").asText());
        assertEquals(strategyId, jsonNode.get("strategyId").asText());
        assertEquals(type, jsonNode.get("type").asText());
        assertEquals(symbol, jsonNode.get("symbol").asText());
        assertNotNull(jsonNode.get("timestamp").asText());
    }

    @Test
    void testTimestampFormat() throws JsonProcessingException {
        TestEvent event = new TestEvent("testStrategy", "TEST", "AAPL");
        String json = event.toJson();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);

        String timestampPattern = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}";
        assertTrue(jsonNode.get("timestamp").asText().matches(timestampPattern));
    }

    private static class TestEvent extends BaseEvent {
        public TestEvent(String strategyId, String type, String symbol) {
            super(strategyId, type, symbol);
        }
    }
}