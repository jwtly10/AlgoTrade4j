package dev.jwtly10.core.strategy.demo;

import dev.jwtly10.core.indicators.iSMA;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.TradeParameters;
import dev.jwtly10.core.strategy.BaseStrategy;
import dev.jwtly10.core.strategy.Parameter;
import lombok.extern.slf4j.Slf4j;

// Last Modified @ 27/09/2024 10:43:00:PM
@Slf4j
public class SMACrossoverStrategy extends BaseStrategy {
    @Parameter(name = "shortSMALength", description = "Length of short-term SMA", value = "10")
    private int shortSMALength;

    @Parameter(name = "longSMALength", description = "Length of long-term SMA", value = "50")
    private int longSMALength;

    private iSMA shortISMA;
    private iSMA longISMA;

    public SMACrossoverStrategy() {
        super("SMACrossoverStrategy");
    }

    public SMACrossoverStrategy(String strategyId) {
        super(strategyId);
    }

    @Override
    protected void initIndicators() {
        shortISMA = createIndicator(iSMA.class, shortSMALength);
        longISMA = createIndicator(iSMA.class, longSMALength);
    }

    @Override
    public void onStart() {
        log.info("SMACrossoverStrategy starting. Strategy ID: {}", getStrategyId());
        log.info("Initial balance: {}", getBalance());
    }

    @Override
    public void onTick(Tick tick, Bar currentBar) {
        // We don't need to do anything on each tick for this strategy
    }

    @Override
    public void onBarClose(Bar bar) {
        if (shortISMA.isReady() && longISMA.isReady()) {
            double shortSMAValue = shortISMA.getValue();
            double longSMAValue = longISMA.getValue();
            double prevShortSMAValue = shortISMA.getValue(1);
            double prevLongSMAValue = longISMA.getValue(1);

            // Check for crossover
//            if (shortSMAValue.isGreaterThan(longSMAValue) && (prevShortSMAValue.isLessThan(prevLongSMAValue) || prevLongSMAValue.isEquals(prevLongSMAValue))) {
//                // Buy signal
//                openLong(createTradeParameters(bar, true));
//            }
//
            if ((shortSMAValue < longSMAValue) && (prevShortSMAValue > prevLongSMAValue) || (prevShortSMAValue == prevLongSMAValue)) {
                // Sell signal
                if (shortSMAValue == 0) {
                    return;
                }
                if (longSMAValue == 0) {
                    return;
                }
                if (prevShortSMAValue == 0) {
                    return;
                }
                if (prevLongSMAValue == 0) {
                    return;
                }
                openShort(createTradeParameters(bar, false));
                log.info("Prev short sma value: {} ", prevShortSMAValue);
                log.info("Short sma value: {} ", shortSMAValue);
                log.info("Prev LONG sma value: {} ", prevLongSMAValue);
                log.info("Long sma value: {} ", longSMAValue);
            }
        }
    }

    private TradeParameters createTradeParameters(Bar bar, boolean isLong) {
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        params.setEntryPrice(isLong ? Ask() : Bid());

        Number candleSize = bar.getHigh().subtract(bar.getLow());
        Number halfCandleSize = candleSize.divide(2);

        Number stopLossPrice;
        if (isLong) {
            stopLossPrice = bar.getClose().subtract(halfCandleSize);
        } else {
            stopLossPrice = bar.getClose().add(halfCandleSize);
        }

        params.setStopLoss(stopLossPrice);
        params.setRiskRatio(2);
        params.setRiskPercentage(1);
        params.setBalanceToRisk(getBalance());

        return params;
    }

    @Override
    public void onEnd() {
        log.info("SMACrossoverStrategy shutting down. Final balance: {} Final Equity: {}", getBalance(), getEquity());
    }
}