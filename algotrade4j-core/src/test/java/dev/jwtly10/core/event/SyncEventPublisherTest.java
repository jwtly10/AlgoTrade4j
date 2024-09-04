package dev.jwtly10.core.event;

import dev.jwtly10.core.model.Instrument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class SyncEventPublisherTest {

    private SyncEventPublisher publisher;

    @Mock
    private EventListener mockListener1;

    @Mock
    private EventListener mockListener2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publisher = new SyncEventPublisher();
    }

    @Test
    void testAddListener() {
        publisher.addListener(mockListener1);
        publisher.publishEvent(new TestEvent("Test Event"));

        verify(mockListener1, times(1)).onEvent(any(TestEvent.class));
    }

    @Test
    void testRemoveListener() {
        publisher.addListener(mockListener1);
        publisher.removeListener(mockListener1);
        publisher.publishEvent(new TestEvent("Test Event"));

        verify(mockListener1, never()).onEvent(any(TestEvent.class));
    }

    @Test
    void testPublishEvent() {
        publisher.addListener(mockListener1);
        publisher.addListener(mockListener2);

        TestEvent testEvent = new TestEvent("Test Event");
        publisher.publishEvent(testEvent);

        verify(mockListener1, times(1)).onEvent(testEvent);
        verify(mockListener2, times(1)).onEvent(testEvent);
    }

    @Test
    void testPublishErrorEvent() {
        publisher.addListener(mockListener1);
        Exception testException = new RuntimeException("Test Exception");
        publisher.publishErrorEvent("TestStrategy", testException);

        verify(mockListener1, times(1)).onError("TestStrategy", testException);
    }

    @Test
    void testMultipleEventsPublished() {
        publisher.addListener(mockListener1);

        int eventCount = 5;
        for (int i = 0; i < eventCount; i++) {
            publisher.publishEvent(new TestEvent("Test Event " + i));
        }

        verify(mockListener1, times(eventCount)).onEvent(any(TestEvent.class));
    }

    @Test
    void testShutdown() {
        // Shutdown method is empty anyway for synchronous events
        publisher.shutdown();
    }

    private static class TestEvent extends BaseEvent {
        private final String message;

        TestEvent(String message) {
            super("Testing", "TEST_EVENT", Instrument.NAS100USD);
            this.message = message;
        }

        @Override
        public String toString() {
            return "TestEvent{message='" + message + "'}";
        }
    }
}