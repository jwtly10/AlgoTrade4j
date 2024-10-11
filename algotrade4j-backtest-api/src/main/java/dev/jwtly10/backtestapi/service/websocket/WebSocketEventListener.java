package dev.jwtly10.backtestapi.service.websocket;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.event.EventListener;
import dev.jwtly10.core.event.types.ErrorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class WebSocketEventListener implements EventListener {
    private static final int COMPRESSION_THRESHOLD = 1024; // 1KB
    private final WebSocketSession session;
    private final Set<Class<? extends BaseEvent>> subscribedEventTypes = new HashSet<>();
    private final AtomicBoolean isActive = new AtomicBoolean(true);
    private final Object lock = new Object();
    private final String strategyId;

    public WebSocketEventListener(WebSocketSession session, String strategyId) {
        this.session = session;
        this.strategyId = strategyId;
    }

    @Override
    public void onEvent(BaseEvent event) {
        if (!isActive.get() || !subscribedEventTypes.contains(event.getClass()) || !event.getStrategyId().equals(this.strategyId)) {
            return;
        }

        synchronized (lock) {
            try {
                if (session.isOpen()) {
                    String jsonMessage = event.toJson();
                    byte[] messageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);

                    ByteBuffer buffer;
                    if (messageBytes.length > COMPRESSION_THRESHOLD) {
                        // Compress large messages
                        byte[] compressedMessage = compressMessage(jsonMessage);
                        buffer = ByteBuffer.allocate(compressedMessage.length + 1);
                        buffer.put((byte) 1); // Flag for compressed message
                        buffer.put(compressedMessage);
                    } else {
                        buffer = ByteBuffer.allocate(messageBytes.length + 1);
                        buffer.put((byte) 0); // Flag for uncompressed message
                        buffer.put(messageBytes);
                    }
                    buffer.flip();
                    session.sendMessage(new BinaryMessage(buffer));
                }
            } catch (IllegalStateException e) {
                log.debug("Session already closed, unable to send event: {}", event);
                deactivate();
            } catch (IOException e) {
                log.error("Failed to send message to WS session", e);
            }
        }
    }

    private byte[] compressMessage(String message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(message.getBytes(StandardCharsets.UTF_8));
        }
        return baos.toByteArray();
    }

    @Override
    public void onError(String strategyId, Exception e) {
        try {
            String errorDetails = formatErrorWithStackTrace(strategyId, e);
            ErrorEvent errorEvent = new ErrorEvent(strategyId, errorDetails);
            session.sendMessage(new TextMessage(errorEvent.toJson()));
        } catch (IOException ex) {
            log.error("Failed to send error message to WS session", ex);
        } catch (Exception ex) {
            log.error("Unexpected error sending error message to WS session", ex);
        }
    }

    @Override
    public void onError(String strategyId, String message) {
        try {
            ErrorEvent errorEvent = new ErrorEvent(strategyId, message);
            session.sendMessage(new TextMessage(errorEvent.toJson()));
        } catch (IOException ex) {
            log.error("Failed to send error message to WS session", ex);
        } catch (Exception ex) {
            log.error("Unexpected error sending error message to WS session", ex);
        }
    }

    public void subscribe(Class<? extends BaseEvent> eventType) {
        subscribedEventTypes.add(eventType);
    }

    public void unsubscribe(Class<? extends BaseEvent> eventType) {
        subscribedEventTypes.remove(eventType);
    }

    public void deactivate() {
        isActive.set(false);
    }

    private String formatErrorWithStackTrace(String strategyId, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Strategy: %s | Error: %s - %s\n",
                strategyId, e.getClass().getSimpleName(), e.getMessage()));

        StackTraceElement[] stackTrace = e.getStackTrace();
        int linesToInclude = Math.min(5, stackTrace.length); // Include up to 5 lines of stack trace

        sb.append("Stack trace:\n");
        for (int i = 0; i < linesToInclude; i++) {
            sb.append("  at ").append(stackTrace[i].toString()).append("\n");
        }

        if (stackTrace.length > linesToInclude) {
            sb.append("  ... ").append(stackTrace.length - linesToInclude).append(" more\n");
        }

        return sb.toString();
    }
}