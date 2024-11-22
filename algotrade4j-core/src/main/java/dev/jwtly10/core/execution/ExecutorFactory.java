package dev.jwtly10.core.execution;

import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.external.news.StrategyNewsUtil;
import dev.jwtly10.core.model.Broker;
import dev.jwtly10.core.strategy.Strategy;

public interface ExecutorFactory {
    BacktestExecutor createExecutor(Broker broker, Strategy strategy, String id, DataManager dataManager, EventPublisher eventPublisher, StrategyNewsUtil strategyNewsUtil, double initialCash);
}