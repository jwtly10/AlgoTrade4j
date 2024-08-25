package dev.jwtly10.core.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EventPublisherTest {

    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = new EventPublisher();
    }

    @Test
    void testAddListener() {
        EventListener listener = mock(EventListener.class);
        eventPublisher.addListener(listener);

        BaseEvent event = mock(BaseEvent.class);
        eventPublisher.publishEvent(event);

        verify(listener, timeout(1000)).onEvent(event);
    }

    @Test
    void testRemoveListener() {
        EventListener listener = mock(EventListener.class);
        eventPublisher.addListener(listener);
        eventPublisher.removeListener(listener);

        BaseEvent event = mock(BaseEvent.class);
        eventPublisher.publishEvent(event);

        verify(listener, after(1000).never()).onEvent(any());
    }

    @Test
    void testPublishEvent() throws InterruptedException {
        int listenerCount = 3;
        CountDownLatch latch = new CountDownLatch(listenerCount);

        for (int i = 0; i < listenerCount; i++) {
            EventListener listener = mock(EventListener.class);
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(listener).onEvent(any());
            eventPublisher.addListener(listener);
        }

        BaseEvent event = mock(BaseEvent.class);
        eventPublisher.publishEvent(event);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Not all listeners were notified");
    }

    @Test
    void testPublishErrorEvent() throws InterruptedException {
        int listenerCount = 3;
        CountDownLatch latch = new CountDownLatch(listenerCount);

        for (int i = 0; i < listenerCount; i++) {
            EventListener listener = mock(EventListener.class);
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(listener).onError(anyString(), any());
            eventPublisher.addListener(listener);
        }

        String strategyId = "testStrategy";
        Exception testException = new RuntimeException("Test exception");
        eventPublisher.publishErrorEvent(strategyId, testException);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Not all listeners were notified of error");
    }
}