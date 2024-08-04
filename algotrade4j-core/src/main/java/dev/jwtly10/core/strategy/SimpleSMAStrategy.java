package dev.jwtly10.core.strategy;


import dev.jwtly10.core.Bar;
import dev.jwtly10.core.BarSeries;
import dev.jwtly10.core.Indicator;
import dev.jwtly10.core.TradeManager;
import dev.jwtly10.core.indicators.SMA;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SimpleSMAStrategy extends BaseStrategy {
    private SMA sma20;

    public SimpleSMAStrategy() {
        super("SimpleSMAStrategy");
    }

    @Override
    protected void initIndicators() {
        sma20 = createIndicator(SMA.class, 20);
    }

    @Override
    public void onStart() {
        log.info("SimpleSMAStrategy starting. Strategy ID: {}", getStrategyId());
        log.info("Initial balance: {}", tradeManager.getAccount().getBalance());
    }

    @Override
    public void onBar(Bar bar, BarSeries series, List<Indicator> indicators, TradeManager tradeManager) {
        String barInfo = String.format("New bar - Time: %s, Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f, Volume: %.2f",
                bar.getDateTime(),
                bar.getOpen().doubleValue(),
                bar.getHigh().doubleValue(),
                bar.getLow().doubleValue(),
                bar.getClose().doubleValue(),
                (double) bar.getVolume());

        log.info(barInfo);

        if (sma20.isReady()) {
            String smaInfo = String.format("SMA(20) value: %.2f", sma20.getValue().doubleValue());
            log.info(smaInfo);

            String comparisonInfo = String.format("Close price (%.2f) is %s SMA(20) (%.2f)",
                    bar.getClose().doubleValue(),
                    bar.getClose().isGreaterThan(sma20.getValue()) ? "above" : "below",
                    sma20.getValue().doubleValue());
            log.info(comparisonInfo);
        }

        log.info("Current balance: {}", tradeManager.getAccount().getBalance());
        log.info("--------------------");
    }

    @Override
    public void onDeInit() {
        log.info("SimpleSMAStrategy shutting down. Final balance: {}", tradeManager.getAccount().getBalance());
    }
}