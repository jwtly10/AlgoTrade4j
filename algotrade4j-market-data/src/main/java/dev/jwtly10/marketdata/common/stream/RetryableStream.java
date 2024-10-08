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
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final long MAX_BACKOFF_MS = 30000;
    protected final ObjectMapper objectMapper;
    protected final OkHttpClient client;
    protected final Request request;
    protected Call call;
    protected BufferedReader reader;
    protected volatile boolean isRunning = false;

    /**
     * Constructor for RetryableStream.
     *
     * @param client       The OkHttpClient instance.
     * @param request      The HTTP request.
     * @param objectMapper The ObjectMapper instance for JSON processing.
     */
    public RetryableStream(OkHttpClient client, Request request, ObjectMapper objectMapper) {
        this.client = client;
        this.request = request;
        this.objectMapper = objectMapper;
    }

    /**
     * Starts the stream with the provided callback.
     *
     * @param callback The callback to handle stream events.
     */
    @Override
    public void start(StreamCallback<T> callback) {
        log.info("Starting stream for class: {}", getClass().getSimpleName());

        isRunning = true;
        retryWithBackoff(callback, 0, INITIAL_BACKOFF_MS);
    }

    /**
     * Retries the stream connection with exponential backoff.
     *
     * @param callback   The callback to handle stream events.
     * @param retryCount The current retry count.
     * @param backoffMs  The current backoff time in milliseconds.
     */
    private void retryWithBackoff(StreamCallback<T> callback, int retryCount, long backoffMs) {
        if (!isRunning) {
            return;
        }

        if (retryCount >= MAX_RETRIES) {
            log.error("Max retries reached for class: {}", getClass().getSimpleName());
            callback.onError(new Exception("Max retries reached"));
            return;
        }

        log.info("Attempting to connect stream for class: {} (retry {}/{})", getClass().getSimpleName(), retryCount + 1, MAX_RETRIES);
        call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Stream connection failed for class: {}", getClass().getSimpleName(), e);
                scheduleRetry(callback, retryCount, backoffMs);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    log.info("Stream connection established successfully");
                }
                try {
                    reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                    String line;
                    while (isRunning && (line = reader.readLine()) != null) {
                        processLine(line, callback);
                    }
                } catch (Exception e) {
                    if (e.getMessage().contains("stream was reset: CANCEL")) {
                        log.debug("Stream for class {} was stopped internally, closing stream", getClass().getSimpleName());
                    } else {
                        log.error("Error processing stream for class: {}", getClass().getSimpleName(), e);
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

        long nextBackoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
        log.info("Scheduling retry {} in {} ms for class: {}", retryCount + 1, backoffMs, getClass().getSimpleName());


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("Retrying stream connection for class: {}", getClass().getSimpleName());
                retryWithBackoff(callback, retryCount + 1, nextBackoffMs);
            }
        }, backoffMs);
    }

    /**
     * Closes the stream.
     */
    @Override
    public void close() {
        log.info("Manually closing stream for class: {}", getClass().getSimpleName());
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
        log.info("Closing resource quietly for class: {}", getClass().getSimpleName());
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("Error closing resource for class: {}", getClass().getSimpleName(), e);
            }
        }
    }
}