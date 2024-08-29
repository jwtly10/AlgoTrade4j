package dev.jwtly10.core.indicators;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.IndicatorEvent;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.IndicatorValue;
import dev.jwtly10.core.model.Number;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class iATR implements Indicator {
    // Params
    private final int period;

    private final List<Number> trueRanges;
    @Getter
    private final List<IndicatorValue> values;
    private final String name;
    private final Number multiplier;
    private String strategyId;
    private EventPublisher eventPublisher;
    private Number previousClose;

    public iATR(int period) {
        this.period = period;
        this.trueRanges = new ArrayList<>();
        this.values = new ArrayList<>();
        this.name = "ATR " + period;
        this.previousClose = null;
        this.multiplier = new Number(2).divide(new Number(period + 1).getValue());

    }

    @Override
    public void update(Bar bar) {
        log.trace("Updating ATR with new bar. High: {}, Low: {}, Close: {}", bar.getHigh(), bar.getLow(), bar.getClose());

        Number trueRange;
        if (previousClose == null) {
            trueRange = bar.getHigh().subtract(bar.getLow());
        } else {
            Number highLow = bar.getHigh().subtract(bar.getLow());
            Number highPrevClose = bar.getHigh().subtract(previousClose).abs();
            Number lowPrevClose = bar.getLow().subtract(previousClose).abs();

            trueRange = highLow;
            if (highPrevClose.isGreaterThan(trueRange)) {
                trueRange = highPrevClose;
            }
            if (lowPrevClose.isGreaterThan(trueRange)) {
                trueRange = lowPrevClose;
            }
        }

        trueRanges.add(trueRange);
        previousClose = bar.getClose();

        if (isReady()) {
            Number atr;
            if (trueRanges.size() == period) {
                atr = trueRange;
            } else {
                //TODO: Make this configurable
                //Subsequent ATRs use EMA smoothing: ATR = (Current TR * multiplier) + (Prior ATR * (1 - multiplier))
                Number priorATR = values.getLast().getValue();
                atr = trueRange.multiply(multiplier.getValue())
                        .add(priorATR.multiply(Number.ONE.subtract(multiplier).getValue()));
            }

            IndicatorValue indicatorValue = new IndicatorValue(atr, bar.getOpenTime());
            values.add(indicatorValue);

            if (eventPublisher != null) {
                log.trace("Publishing ATR event. Strategy ID: {}, Symbol: {}, Indicator: {}, Value: {}, Timestamp: {}",
                        strategyId, bar.getInstrument(), getName(), atr, bar.getOpenTime());
                eventPublisher.publishEvent(new IndicatorEvent(strategyId, bar.getInstrument(), getName(), indicatorValue));
            }
        } else {
            IndicatorValue indicatorValue = new IndicatorValue(Number.ZERO, bar.getOpenTime());
            values.add(indicatorValue);
            if (eventPublisher != null) {
                eventPublisher.publishEvent(new IndicatorEvent(strategyId, bar.getInstrument(), getName(), indicatorValue));
            }
        }

        // Remove oldest true range if we have more than we need
        if (trueRanges.size() > period) {
            trueRanges.remove(0);
        }
    }

    @Override
    public Number getValue() {
        return values.isEmpty() ? Number.ZERO : values.getLast().getValue();
    }

    @Override
    public Number getValue(int index) {
        if (index < 0 || index >= values.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + values.size());
        }
        return values.get(values.size() - index - 1).getValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isReady() {
        log.trace("Checking if ATR is ready. Values size: {}, Period: {}", trueRanges.size(), period);
        return trueRanges.size() >= period;
    }

    @Override
    public int getRequiredPeriods() {
        return period;
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }
}