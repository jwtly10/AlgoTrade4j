package dev.jwtly10.core.execution;

import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.strategy.Strategy;

public interface ExecutorFactory {
    BacktestExecutor createExecutor(Strategy strategy, String id, DataManager dataManager, EventPublisher eventPublisher, double initialCash);
}