package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.risk.RiskManager;
import dev.jwtly10.core.strategy.Strategy;

public class DefaultExecutorFactory implements ExecutorFactory {
    @Override
    public BacktestExecutor createExecutor(Strategy strategy, String id, DataManager dataManager, EventPublisher eventPublisher, double initialCash) {
        Tick currentTick = new DefaultTick();
        AccountManager accountManager = new DefaultAccountManager(
                initialCash,
                initialCash,
                initialCash
        );
        RiskManager riskManager = new RiskManager(strategy.getRiskProfileConfig(), accountManager, eventPublisher, strategy.getStrategyId(), dataManager.getFrom());
        TradeManager tradeManager = new DefaultTradeManager(currentTick, dataManager.getBarSeries(), id, eventPublisher, riskManager);
        TradeStateManager tradeStateManager = new DefaultTradeStateManager(id, eventPublisher);
        PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();

        return new BacktestExecutor(
                strategy,
                tradeManager,
                tradeStateManager,
                accountManager,
                dataManager,
                dataManager.getBarSeries(),
                eventPublisher,
                performanceAnalyser,
                riskManager
        );
    }
}