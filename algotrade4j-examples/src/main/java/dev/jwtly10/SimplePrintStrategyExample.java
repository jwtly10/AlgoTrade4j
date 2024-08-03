package dev.jwtly10;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.Strategy;
import dev.jwtly10.core.StrategyExecutor;
import dev.jwtly10.core.datafeed.*;
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

        Strategy strategy = new SimplePrintStrategy();
        Number initialCash = new Number("10000");
        int barSeriesSize = 4000;

        EventPublisher eventPublisher = new EventPublisher();

        StrategyExecutor executor = new StrategyExecutor(strategy, dataFeed, initialCash, barSeriesSize, eventPublisher);

        try {
            executor.run();
        } catch (DataFeedException e) {
            log.error("Error running strategy", e);
        }
    }
}