package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.external.news.StrategyNewsUtil;
import dev.jwtly10.core.model.Broker;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.risk.BacktestRiskManager;
import dev.jwtly10.core.strategy.Strategy;

public class DefaultExecutorFactory implements ExecutorFactory {
    @Override
    public BacktestExecutor createExecutor(Broker broker, Strategy strategy, String id, DataManager dataManager, EventPublisher eventPublisher, StrategyNewsUtil strategyNewsUtil, double initialCash) {
        Tick currentTick = new DefaultTick();
        AccountManager accountManager = new DefaultAccountManager(
                initialCash,
                initialCash,
                initialCash
        );
        BacktestRiskManager riskManagementService = new BacktestRiskManager();

        TradeManager tradeManager = new BacktestTradeManager(broker, currentTick, dataManager.getBarSeries(), id, eventPublisher);
        TradeStateManager tradeStateManager = new BacktestTradeStateManager(id, eventPublisher);
        PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();
        return new BacktestExecutor(
                strategy,
                tradeManager,
                tradeStateManager,
                accountManager,
                dataManager,
                dataManager.getBarSeries(),
                eventPublisher,
                riskManagementService,
                performanceAnalyser,
                strategyNewsUtil
        );
    }
}