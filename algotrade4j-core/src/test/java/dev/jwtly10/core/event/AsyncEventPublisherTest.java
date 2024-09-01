package dev.jwtly10.core.event;

import dev.jwtly10.core.model.Instrument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AsyncEventPublisherTest {

    private AsyncEventPublisher publisher;

    @Mock
    private EventListener mockListener1;

    @Mock
    private EventListener mockListener2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publisher = new AsyncEventPublisher();
    }

    @Test
    void testAddListener() {
        publisher.addListener(mockListener1);
        publisher.publishEvent(new TestEvent("Test Event"));

        verify(mockListener1, timeout(1000).times(1)).onEvent(any(TestEvent.class));
    }

    @Test
    void testRemoveListener() {
        publisher.addListener(mockListener1);
        publisher.removeListener(mockListener1);
        publisher.publishEvent(new TestEvent("Test Event"));

        verify(mockListener1, after(500).never()).onEvent(any(TestEvent.class));
    }

    @Test
    void testPublishEvent() throws InterruptedException {
        publisher.addListener(mockListener1);
        publisher.addListener(mockListener2);

        CountDownLatch latch = new CountDownLatch(2);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockListener1).onEvent(any(TestEvent.class));
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockListener2).onEvent(any(TestEvent.class));

        publisher.publishEvent(new TestEvent("Test Event"));

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        verify(mockListener1, times(1)).onEvent(any(TestEvent.class));
        verify(mockListener2, times(1)).onEvent(any(TestEvent.class));
    }

    @Test
    void testBatchProcessing() throws InterruptedException {
        publisher.addListener(mockListener1);

        int eventCount = 200 * 2; // The batch size of the async publisher
        CountDownLatch latch = new CountDownLatch(eventCount);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockListener1).onEvent(any(TestEvent.class));

        for (int i = 0; i < eventCount; i++) {
            publisher.publishEvent(new TestEvent("Test Event " + i));
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockListener1, times(eventCount)).onEvent(any(TestEvent.class));
    }

    @Test
    void testPublishErrorEvent() {
        publisher.addListener(mockListener1);
        Exception testException = new RuntimeException("Test Exception");
        publisher.publishErrorEvent("TestStrategy", testException);

        verify(mockListener1, times(1)).onError("TestStrategy", testException);
    }

    @Test
    void testShutdown() throws InterruptedException {
        publisher.shutdown();
        publisher.addListener(mockListener1);
        publisher.publishEvent(new TestEvent("Test Event"));

        Thread.sleep(500); // Give some time for potential processing
        verify(mockListener1, never()).onEvent(any(TestEvent.class));
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