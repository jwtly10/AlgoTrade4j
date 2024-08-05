package dev.jwtly10;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.*;
import dev.jwtly10.core.backtest.BacktestPriceFeed;
import dev.jwtly10.core.backtest.BacktestTradeManager;
import dev.jwtly10.core.datafeed.*;
import dev.jwtly10.core.defaults.DefaultBarSeries;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.strategy.SimplePrintStrategy;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class SimplePrintStrategyExample {
    public static void main(String[] args) {
        CsvParseFormat format = new DefaultCsvFormat(Duration.ofDays(1));
        // TODO: Dont hardcode path, have a specific directory for example data
        DataFeed dataFeed = new CsvDataFeed("NAS100_USD", "/Users/personal/Projects/AlgoTrade4j/algotrade4j-core/src/main/resources/nas100USD_1D_testdata.csv", format, DataFeedSpeed.INSTANT);

        StrategyExecutor executor = getStrategyExecutor(dataFeed);

        try {
            executor.run();
        } catch (DataFeedException e) {
            log.error("Error running strategy", e);
        }
    }

    private static StrategyExecutor getStrategyExecutor(DataFeed dataFeed) {
        Strategy strategy = new SimplePrintStrategy();
        Number initialCash = new Number("10000");
        int barSeriesSize = 4000;
        BarSeries barSeries = new DefaultBarSeries(barSeriesSize);
        PriceFeed priceFeed = new BacktestPriceFeed(barSeries, new Number(10));

        EventPublisher eventPublisher = new EventPublisher();

        TradeManager tradeManager = new BacktestTradeManager(strategy.getStrategyId(), initialCash, priceFeed, eventPublisher);

        StrategyExecutor executor = new StrategyExecutor(strategy, tradeManager, priceFeed, barSeries, dataFeed, eventPublisher);
        return executor;
    }
}