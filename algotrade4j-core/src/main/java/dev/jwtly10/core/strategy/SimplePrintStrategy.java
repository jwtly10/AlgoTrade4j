package dev.jwtly10.core.strategy;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SimplePrintStrategy implements Strategy {
    private String strategyId = "SimplePrintStrategy";

    private PriceFeed priceFeed;

    public SimplePrintStrategy(String strategyId) {
        this.strategyId = strategyId;
    }

    public SimplePrintStrategy() {
    }

    @Override
    public void onInit(BarSeries series, PriceFeed priceFeed, List<Indicator> indicators, TradeManager tradeManager) {
        log.info("Strategy initialized. Initial bar count: {}", series.getBarCount());
        this.priceFeed = priceFeed;
    }

    @Override
    public void onBar(Bar bar, BarSeries series, List<Indicator> indicators, TradeManager tradeManager) {
        log.info("New bar received: {}", formatBar(bar));

        // Randomly decide to open a trade
        if (Math.random() > 0.5) {
            if (Math.random() > 0.5) {
                if (priceFeed.getBid("NAS100_USD").isGreaterThan(new Number(14300))) {
                    tradeManager.openShortPosition(bar.getSymbol(), new Number(10), new Number(17000), new Number(14000));
                }
            }
        }
    }

    @Override
    public void onDeInit() {
        log.info("Strategy de-initialized.");
    }

    @Override
    public String getStrategyId() {
        return strategyId;
    }

    private String formatBar(Bar bar) {
        return String.format("Time: %s, Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f, Volume: %.2f",
                bar.getDateTime(),
                bar.getOpen().doubleValue(),
                bar.getHigh().doubleValue(),
                bar.getLow().doubleValue(),
                bar.getClose().doubleValue(),
                (double) bar.getVolume());
    }
}