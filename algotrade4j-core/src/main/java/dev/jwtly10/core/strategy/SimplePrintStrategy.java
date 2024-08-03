package dev.jwtly10.core.strategy;

import dev.jwtly10.core.*;
import dev.jwtly10.core.Number;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SimplePrintStrategy implements Strategy {

    @Override
    public void onInit(BarSeries series, List<Indicator> indicators, TradeManager tradeManager) {
        log.info("Strategy initialized. Initial bar count: {}", series.getBarCount());
    }

    @Override
    public void onBar(Bar bar, BarSeries series, List<Indicator> indicators, TradeManager tradeManager) {
        log.info("New bar received: {}", formatBar(bar));

        // Randomly decide to open a trade
        if (Math.random() > 0.5) {
            if (Math.random() > 0.5) {
                tradeManager.openShortPosition(bar.getSymbol(), new Number(10000), new Number(2), new Number(1), TradeManager.BALANCE_TYPE.EQUITY);
            } else {
                tradeManager.openLongPosition(bar.getSymbol(), new Number(100), new Number(2), new Number(1), TradeManager.BALANCE_TYPE.EQUITY);
            }
        }
    }

    @Override
    public void onDeInit() {
        log.info("Strategy de-initialized.");
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