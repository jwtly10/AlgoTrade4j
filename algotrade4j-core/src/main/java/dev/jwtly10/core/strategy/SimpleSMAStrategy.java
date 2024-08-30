package dev.jwtly10.core.strategy;


import dev.jwtly10.core.indicators.iSMA;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.TradeParameters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleSMAStrategy extends BaseStrategy {
    @Parameter(name = "smaLength", description = "Length of SMA", value = "20")
    private int smaLength;

    @Parameter(name = "smaLength2", description = "Length of SMA", value = "20")
    private int smaLength2;

    private iSMA iSma20;

    public SimpleSMAStrategy() {
        super("SimpleSMAStrategy");
    }

    public SimpleSMAStrategy(String strategyId) {
        super(strategyId);
    }

    @Override
    protected void initIndicators() {
        iSma20 = createIndicator(iSMA.class, smaLength);
    }

    @Override
    public void onStart() {
        log.info("SimpleSMAStrategy starting. Strategy ID: {}", getStrategyId());
        log.info("Initial balance of run: {}", getBalance());
    }

    @Override
    public void onTick(Tick tick, Bar currentBar) {
    }

    @Override
    public void onBarClose(Bar bar) {
        if (iSma20.isReady()) {
            // If bearish candle below the sma sell
            if (bar.getClose().getValue().doubleValue() < iSma20.getValue() && bar.isBearish() && bar.getClose().isGreaterThan(new Number(13200))) {
                TradeParameters params = new TradeParameters();
                params.setInstrument(SYMBOL);
                params.setEntryPrice(Ask());
                params.setStopLoss(new Number("15600"));
                params.setRiskRatio(new Number("2"));
                params.setRiskPercentage(new Number("20"));
                params.setBalanceToRisk(getInitialBalance());
                var tradeID = openShort(params);
            }
        }
    }

    @Override
    public void onEnd() {
        log.info("SimpleSMAStrategy shutting down. Final balance: {} Final Equity: {}", getBalance(), getEquity());
    }
}