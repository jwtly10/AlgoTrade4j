package dev.jwtly10.core.strategy;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.TradeParameters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimplePrintStrategy extends BaseStrategy {
    public SimplePrintStrategy() {
        super("SimplePrintStrategy");
    }

    @Override
    public void onStart() {
        log.info("Strategy initialized.");
    }

    @Override
    public void onBarClose(Bar bar) {
        log.info("New bar received: {}", formatBar(bar));

        // Randomly decide to open a trade
        if (Math.random() > 0.5) {
            if (Math.random() > 0.5) {
                if (Bid().isGreaterThan(new Number(14300))) {
                    TradeParameters params = new TradeParameters();
                    params.setSymbol(SYMBOL);
                    params.setEntryPrice(new Number("10"));
                    params.setStopLoss(new Number("14000"));
                    params.setRiskRatio(new Number("2"));
                    params.setRiskPercentage(new Number("10"));
                    // TODO: We need an account manager interface
                    params.setBalanceToRisk(new Number("10000"));
                    var tradeID = openShort(params);
                    log.info("Opened short position: {}", tradeID);
                }
            }
        }
    }

    @Override
    public void onTick(Tick tick, Bar currentBar) {

    }

    @Override
    public void onEnd() {
        log.info("Strategy de-initialized.");
    }

    private String formatBar(Bar bar) {
        return String.format("Time: %s, Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f, Volume: %.2f",
                bar.getOpenTime(),
                bar.getOpen().doubleValue(),
                bar.getHigh().doubleValue(),
                bar.getLow().doubleValue(),
                bar.getClose().doubleValue(),
                bar.getVolume().doubleValue());
    }
}