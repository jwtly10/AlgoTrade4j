package dev.jwtly10.core.strategy;


import dev.jwtly10.core.Number;
import dev.jwtly10.core.*;
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
            // If bearish candle below the sma sell
            if (bar.getClose().isLessThan(sma20.getValue()) && bar.isBearish() && bar.getClose().isGreaterThan(new Number(13200))) {
                tradeManager.openShortPosition(bar.getSymbol(), new Number(10), new Number(17000), new Number(13200));
            }
        }

        log.info("Current balance: {}", tradeManager.getAccount().getBalance());
        log.info("--------------------");
    }

    @Override
    public void onDeInit() {
        log.info("SimpleSMAStrategy shutting down. Final balance: {}", tradeManager.getAccount().getBalance());
    }
}