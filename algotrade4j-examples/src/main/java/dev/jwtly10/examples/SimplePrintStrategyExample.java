package dev.jwtly10.examples;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.account.DefaultAccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.CSVDataProvider;
import dev.jwtly10.core.data.DefaultDataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.*;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.strategy.SimplePrintStrategy;
import dev.jwtly10.core.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class SimplePrintStrategyExample {
    public static void main(String[] args) {
        Duration period = Duration.ofDays(1);
        CSVDataProvider csvDataProvider = new CSVDataProvider(
                "/Users/personal/Projects/AlgoTrade4j/algotrade4j-core/src/main/resources/nas100USD_1D_testdata.csv",
                4,
                new Number(0.1),
                period,
                "NAS100USD"
        );

        BarSeries barSeries = new DefaultBarSeries(4000);

        Strategy strategy = new SimplePrintStrategy();
        DefaultDataManager dataManager = new DefaultDataManager("NAS100USD", csvDataProvider, period, barSeries);

        Tick currentTick = new DefaultTick();

        EventPublisher eventPublisher = new EventPublisher();

        TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, strategy.getStrategyId(), eventPublisher);

        AccountManager accountManager = new DefaultAccountManager(new Number(10000), new Number(10000), new Number(10000));

        TradeStateManager tradeStateManager = new DefaultTradeStateManager(strategy.getStrategyId(), eventPublisher);

        PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();


        BacktestExecutor executor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);

        dataManager.addDataListener(executor);

        try {
            executor.run();
        } catch (Exception e) {
            log.error("Error running strategy", e);
        }
    }
}