package dev.jwtly10.core.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.model.Instrument;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEventTest {

    @Test
    void testConstructor() {
        String strategyId = "testStrategy";
        String type = "TEST";
        Instrument instrument = Instrument.NAS100USD;

        TestEvent event = new TestEvent(strategyId, type, instrument);

        assertTrue(true);
        assertEquals(strategyId, event.getStrategyId());
        assertEquals(type, event.getType());
        assertEquals(instrument, event.getInstrument());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testTimestampIsCurrentTime() {
        TestEvent event = new TestEvent("testStrategy", "TEST", Instrument.NAS100USD);
        ZonedDateTime now = ZonedDateTime.now();

        // Allow for a small time difference (e.g., 1 second) due to execution time
        assertTrue(Math.abs(event.getTimestamp().toEpochSecond() - now.toEpochSecond()) <= 1);
    }

    @Test
    void testToJson() throws JsonProcessingException {
        String strategyId = "testStrategy";
        String type = "TEST";
        Instrument instrument = Instrument.NAS100USD;

        TestEvent event = new TestEvent(strategyId, type, instrument);
        String json = event.toJson();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);

        assertEquals(strategyId, jsonNode.get("strategyId").asText());
        assertEquals(type, jsonNode.get("type").asText());
        assertEquals(instrument.toString(), jsonNode.get("instrument").asText());
        assertNotNull(jsonNode.get("timestamp").asText());
    }

    private static class TestEvent extends BaseEvent {
        public TestEvent(String strategyId, String type, Instrument instrument) {
            super(strategyId, type, instrument);
        }
    }
}