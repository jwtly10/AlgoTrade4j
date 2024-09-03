package dev.jwtly10.core.event;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LogEventTest {

    @Test
    void testInfoLogEvent() {
        LogEvent event = new LogEvent("strategy1", LogEvent.LogType.INFO, "This is an info message");

        assertEquals("strategy1", event.getStrategyId());
        assertEquals("LOG", event.getType());
        assertEquals(LogEvent.LogType.INFO, event.getLogType());
        assertEquals("This is an info message", event.getMessage());
        assertNotNull(event.getTime());
        assertTrue(event.getTime().isBefore(ZonedDateTime.now()) || event.getTime().isEqual(ZonedDateTime.now()));
    }

    @Test
    void testWarningLogEvent() {
        LogEvent event = new LogEvent("strategy2", LogEvent.LogType.WARNING, "Warning: {} occurred", "Low memory");

        assertEquals("strategy2", event.getStrategyId());
        assertEquals("LOG", event.getType());
        assertEquals(LogEvent.LogType.WARNING, event.getLogType());
        assertEquals("Warning: Low memory occurred", event.getMessage());
    }

    @Test
    void testErrorLogEvent() {
        Exception testException = new RuntimeException("Test exception");
        LogEvent event = new LogEvent("strategy3", LogEvent.LogType.ERROR, "Error occurred: {}", testException, "Database connection failed");

        assertEquals("strategy3", event.getStrategyId());
        assertEquals("LOG", event.getType());
        assertEquals(LogEvent.LogType.ERROR, event.getLogType());
        assertEquals("Error occurred: Database connection failed - Error: Test exception", event.getMessage());
    }

    @Test
    void testLogEventWithMultipleParams() {
        LogEvent event = new LogEvent("strategy4", LogEvent.LogType.INFO, "Value1: {}, Value2: {}", "Hello", 42);

        assertEquals("strategy4", event.getStrategyId());
        assertEquals("LOG", event.getType());
        assertEquals(LogEvent.LogType.INFO, event.getLogType());
        assertEquals("Value1: Hello, Value2: 42", event.getMessage());
    }

    @Test
    void testLogEventWithNoParams() {
        LogEvent event = new LogEvent("strategy5", LogEvent.LogType.INFO, "Static message");

        assertEquals("strategy5", event.getStrategyId());
        assertEquals("LOG", event.getType());
        assertEquals(LogEvent.LogType.INFO, event.getLogType());
        assertEquals("Static message", event.getMessage());
    }

    @Test
    void testLogEventWithNullException() {
        LogEvent event = new LogEvent("strategy6", LogEvent.LogType.ERROR, "Error message", null, "Some context");

        assertEquals("strategy6", event.getStrategyId());
        assertEquals("LOG", event.getType());
        assertEquals(LogEvent.LogType.ERROR, event.getLogType());
        assertEquals("Error message", event.getMessage());
    }
}