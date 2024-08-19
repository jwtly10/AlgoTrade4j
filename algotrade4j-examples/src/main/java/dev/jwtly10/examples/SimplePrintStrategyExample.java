package dev.jwtly10.examples;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimplePrintStrategyExample {
    public static void main(String[] args) {
/*        Duration period = Duration.ofDays(1);
        CSVDataProvider csvDataProvider = new CSVDataProvider(
                "/Users/personal/Projects/AlgoTrade4j/algotrade4j-core/src/main/resources/nas100USD_1D_testdata.csv",
                4,
                new Number(0.1),
                period,
                "NAS100USD"
        );
        csvDataProvider.setDataSpeed(DataSpeed.INSTANT);

        BarSeries barSeries = new DefaultBarSeries(4000);
        Tick currentTick = new DefaultTick();


        Strategy strategy = new SimplePrintStrategy();

        DefaultDataManager dataManager = new DefaultDataManager("NAS100USD", csvDataProvider, period, barSeries);

        EventPublisher eventPublisher = new EventPublisher();

        TradeManager tradeManager = new DefaultTradeManager(currentTick, barSeries, strategy.getStrategyId(), eventPublisher);

        AccountManager accountManager = new DefaultAccountManager(new Number(10000), new Number(10000), new Number(10000));

        TradeStateManager tradeStateManager = new DefaultTradeStateManager(strategy.getStrategyId(), eventPublisher);

        PerformanceAnalyser performanceAnalyser = new PerformanceAnalyser();

        BacktestExecutor executor = new BacktestExecutor(strategy, tradeManager, tradeStateManager, accountManager, dataManager, barSeries, eventPublisher, performanceAnalyser);
        executor.initialise();

        dataManager.addDataListener(executor);

        try {
            dataManager.start();
        } catch (Exception e) {
            log.error("Error running strategy", e);
        }*/
    }
}