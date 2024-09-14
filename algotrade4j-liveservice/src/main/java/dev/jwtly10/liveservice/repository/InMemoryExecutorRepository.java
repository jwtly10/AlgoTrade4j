package dev.jwtly10.liveservice.repository;

import dev.jwtly10.liveservice.executor.LiveExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryExecutorRepository {
    private final Map<String, LiveExecutor> runningStrategies = new ConcurrentHashMap<>();

    public void addStrategy(String strategyName, LiveExecutor executor) {
        runningStrategies.put(strategyName, executor);
    }

    public LiveExecutor getStrategy(String strategyName) {
        return runningStrategies.get(strategyName);
    }

    public LiveExecutor removeStrategy(String strategyName) {
        return runningStrategies.remove(strategyName);
    }

    public boolean containsStrategy(String strategyName) {
        return runningStrategies.containsKey(strategyName);
    }
}