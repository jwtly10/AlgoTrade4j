package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.strategy.Strategy;

public class DefaultExecutorFactory implements ExecutorFactory {
    @Override
    public BacktestExecutor createExecutor(Strategy strategy, String id, DataManager dataManager, EventPublisher eventPublisher, Number initialCash) {
        Tick currentTick = new DefaultTick();
        TradeManager tradeManager = new DefaultTradeManager(currentTick, dataManager.getBarSeries(), id, eventPublisher);
        AccountManager accountManager = new DefaultAccountManager(
                initialCash,
                initialCash,
                initialCash
        );
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
                performanceAnalyser
        );
    }
}