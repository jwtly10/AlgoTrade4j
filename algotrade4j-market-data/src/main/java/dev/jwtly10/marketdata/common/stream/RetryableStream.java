package dev.jwtly10.marketdata.common.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Abstract class representing a retryable stream of data.
 *
 * @param <T> The type of data being streamed.
 */
@Slf4j
public abstract class RetryableStream<T> implements Stream<T> {
    private static final int MAX_RETRIES = 10;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final long MAX_BACKOFF_MS = 30000;
    protected final ObjectMapper objectMapper;
    protected final OkHttpClient client;
    protected final Request request;
    protected final Timer timer;
    protected Call call;
    protected BufferedReader reader;
    protected volatile boolean isRunning = false;
    private int retryCount = 0;

    private final String streamClassName;


    /**
     * Constructor for RetryableStream.
     *
     * @param client       The OkHttpClient instance.
     * @param request      The HTTP request.
     * @param objectMapper The ObjectMapper instance for JSON processing.
     * @param timer        The Timer instance for scheduling retries.
     */
    public RetryableStream(OkHttpClient client, Request request, ObjectMapper objectMapper, Timer timer) {
        this.client = client;
        this.request = request;
        this.objectMapper = objectMapper;
        this.timer = timer;
        this.streamClassName = getClass().getSimpleName();
    }

    /**
     * Constructor for RetryableStream. Uses default Timer instance.
     *
     * @param client       The OkHttpClient instance.
     * @param request      The HTTP request.
     * @param objectMapper The ObjectMapper instance for JSON processing.
     */
    public RetryableStream(OkHttpClient client, Request request, ObjectMapper objectMapper) {
        this(client, request, objectMapper, new Timer(true));
    }

    /**
     * Starts the stream with the provided callback.
     *
     * @param callback The callback to handle stream events.
     */
    @Override
    public void start(StreamCallback<T> callback) {
        log.info("Starting stream for class: {}", streamClassName);

        isRunning = true;
        retryWithBackoff(callback, INITIAL_BACKOFF_MS);
    }

    /**
     * Retries the stream connection with exponential backoff.
     *
     * @param callback  The callback to handle stream events.
     * @param backoffMs The current backoff time in milliseconds.
     */
    private void retryWithBackoff(StreamCallback<T> callback, long backoffMs) {
        if (!isRunning) {
            log.warn("Stream attempted to retry when not running for class: {}", streamClassName);
            return;
        }

        if (retryCount >= MAX_RETRIES) {
            log.error("Max retries reached for class: {}", streamClassName);
            callback.onError(new Exception("Max retries reached"));
            return;
        }

        log.info("Connecting to stream for class: {} (attempt {}/{})", streamClassName, retryCount + 1, MAX_RETRIES);

        call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Stream connection failed for class: {}", streamClassName, e);
                scheduleRetry(callback, retryCount, backoffMs);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    log.info("Stream connection established successfully for class: {}", streamClassName);
                    retryCount = 0;
                } else {
                    log.error("Stream connection failed with status code: {} for class: {}", response.code(), streamClassName);
                    scheduleRetry(callback, retryCount, backoffMs);
                    return;
                }

                try {
                    reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                    String line = "";
                    log.debug("Starting stream read loop for class: {}", streamClassName);
                    while (isRunning && (line = reader.readLine()) != null) {
                        processLine(line, callback);
                    }

                    // TODO: Remove this.
                    // These log messages are to debug an issue we have in production with the stream ending unexpectedly
                    // Its very hard to debug as it only happens after a number of hours running, so this is a temporary
                    // measure to try and get more information.
                    // 20/11 I believe the issue is because for some reason the stream is being closed by the server
                    // and we still think its running so getting to this point in this try statement is unexpected
                    // and we should retry
                    if (!isRunning) {
                        log.debug("Stream loop ended because isRunning=false for class: {}", streamClassName);
                    } else if (line == null) {
                        log.debug("Stream loop ended because readLine() returned null for class: {}", streamClassName);
                    } else {
                        log.debug("Stream loop ended for unknown reason. isRunning={}, line={} for class: {}", isRunning, line, streamClassName);
                    }

                    if (isRunning) {
                        log.warn("Stream loop ended while isRunning=True - this indicates an unexpected disconnection for class: {}", streamClassName);
                        // You might also want to add connection state info here
                        log.debug("Connection state - call.isCanceled(): {}. For class: {}", call.isCanceled(), streamClassName);
                        try {
                            log.debug("Reader state - reader.ready(): {}. For class: ", reader.ready(), streamClassName);
                        } catch (IOException e) {
                            log.debug("Reader is in error state: {}. For class: {}", e.getMessage(), streamClassName);
                        }

                        // isRunning means we expect the stream to be running, so we should retry
                        scheduleRetry(callback, retryCount, backoffMs);
                    }

                } catch (Exception e) {
                    if (e.getMessage().contains("stream was reset: CANCEL")) {
                        log.debug("Stream was stopped internally, closing stream for class: {}", streamClassName);
                    } else {
                        log.error("Error processing stream for class: {}: '{}'", streamClassName, e.getMessage(), e);
                    }
                    scheduleRetry(callback, retryCount, backoffMs);
                } finally {
                    if (isRunning) {
                        closeQuietly(reader);
                    }
                }
            }
        });
    }

    /**
     * Processes a line of data from the stream.
     *
     * @param line     The line of data.
     * @param callback The callback to handle stream events.
     * @throws Exception If an error occurs while processing the line.
     */
    protected abstract void processLine(String line, StreamCallback<T> callback) throws Exception;

    /**
     * Schedules a retry with exponential backoff.
     *
     * @param callback   The callback to handle stream events.
     * @param retryCount The current retry count.
     * @param backoffMs  The current backoff time in milliseconds.
     */
    private void scheduleRetry(StreamCallback<T> callback, int retryCount, long backoffMs) {
        if (!isRunning) {
            return;
        }

        retryCount++;
        long nextBackoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
        log.info("Scheduling retry {} in {} ms for class: {}", retryCount + 1, backoffMs, streamClassName);


        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                retryWithBackoff(callback, nextBackoffMs);
            }
        }, backoffMs);
    }

    /**
     * Closes the stream.
     */
    @Override
    public void close() {
        log.info("Manually closing stream for class: {}", streamClassName);
        isRunning = false;
        if (call != null) {
            call.cancel();
        }
        closeQuietly(reader);
    }

    /**
     * Closes a Closeable resource quietly.
     *
     * @param closeable The Closeable resource.
     */
    private void closeQuietly(Closeable closeable) {
        log.info("Closing resource quietly for class: {}", streamClassName);
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("Error closing resource for class: {}", streamClassName, e);
            }
        }
    }

    protected int getRetryCount() {
        return retryCount;
    }
}