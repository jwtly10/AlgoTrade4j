package dev.jwtly10.core.strategy;

import dev.jwtly10.core.indicators.iATRCandle;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ATRStrategy extends BaseStrategy {
    @Parameter(name = "atrLength", description = "Length of ATR", value = "20")
    private int atrLength;

    @Parameter(name = "atrSensitivity", description = "ATR Sensitivity", value = "1.6")
    private double candleMulti;

    @Parameter(name = "relativeSize", description = "Relative Size Diff", value = "2")
    private double relativeSize;

    @SuppressWarnings("unused")
    private iATRCandle atrCandle;

    public ATRStrategy() {
        super("ATRStrategy");
    }

    public ATRStrategy(String strategyId) {
        super(strategyId);
    }

    @Override
    protected void initIndicators() {
        atrCandle = createIndicator(iATRCandle.class, atrLength, candleMulti, relativeSize);
        // atr = createIndicator(iATR.class, atrLength);
    }

    @Override
    public void onTick(Tick tick, Bar currentBar) {
    }

    @Override
    public void onBarClose(Bar bar) {
    }
}