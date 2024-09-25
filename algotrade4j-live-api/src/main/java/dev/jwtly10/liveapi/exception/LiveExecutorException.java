package dev.jwtly10.liveapi.exception;

public class LiveExecutorException extends RuntimeException {
    private final String strategyId;

    public LiveExecutorException(String strategyId, String message) {
        super(message);
        this.strategyId = strategyId;
    }

    public LiveExecutorException(String errorId, String message, Throwable cause) {
        super(message, cause);
        this.strategyId = errorId;
    }

    public String getErrorId() {
        return strategyId;
    }
}