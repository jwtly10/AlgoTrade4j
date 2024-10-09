package dev.jwtly10.marketdata.common.stream;

/**
 * Represents a stream of data.
 *
 * @param <T> The type of data being streamed.
 */
public interface Stream<T> {

    /**
     * Starts the stream with the provided callback.
     *
     * @param callback The callback to handle stream events.
     */
    void start(StreamCallback<T> callback);

    /**
     * Closes the stream.
     */
    void close();

    /**
     * Callback interface for handling stream events.
     *
     * @param <T> The type of data being streamed.
     */
    interface StreamCallback<T> {

        /**
         * Called when new data is received.
         *
         * @param data The received data.
         */
        void onData(T data);

        /**
         * Called when an error occurs in the stream.
         *
         * @param e The exception that occurred.
         */
        void onError(Exception e);

        /**
         * Called when the stream is complete.
         */
        void onComplete();
    }
}