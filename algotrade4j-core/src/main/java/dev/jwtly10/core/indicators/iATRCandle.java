package dev.jwtly10.core.indicators;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.IndicatorEvent;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.IndicatorValue;
import dev.jwtly10.core.model.Number;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class iATRCandle implements Indicator {
    // Params
    private final int atrPeriod;
    private final double atrMultiplier;
    private final double relativeSize;

    private final iATR atr;
    @Getter
    private final List<IndicatorValue> values;
    private final String name;
    private Bar prevBar;
    private String strategyId;
    private EventPublisher eventPublisher;

    public iATRCandle(int atrPeriod, double sensitivity, double relativeSize) {
        this.atrPeriod = atrPeriod;
        this.atrMultiplier = sensitivity;
        this.atr = new iATR(atrPeriod);
        this.values = new ArrayList<>();
        this.name = "ATR_CANDLE " + atrPeriod + " " + sensitivity;
        this.relativeSize = relativeSize;
    }

    @Override
    public void update(Bar bar) {
        atr.update(bar);

        if (atr.isReady()) {
            boolean violation = checkViolation(bar);
            IndicatorValue indicatorValue = new IndicatorValue(violation ? 1 : 0, bar.getOpenTime());
            values.add(indicatorValue);

            if (violation) {
                if (eventPublisher != null) {
                    log.trace("Publishing ATR Violation event. Strategy ID: {}, Symbol: {}, Indicator: {}, Timestamp: {}",
                            strategyId, bar.getInstrument(), getName(), bar.getOpenTime());
                    eventPublisher.publishEvent(new IndicatorEvent(strategyId, bar.getInstrument(), getName(), indicatorValue));
                }
            }
        } else {
            values.add(new IndicatorValue(0, bar.getOpenTime()));
        }

        prevBar = bar;
    }

    private boolean checkViolation(Bar currentBar) {
        Number absDif = (currentBar.getClose().subtract(currentBar.getOpen())).abs();
        double atrThreshold = atr.getValue() * atrMultiplier;

        boolean atrViolation = absDif.compareTo(new Number(atrThreshold)) > 0;

        if (prevBar == null) {
            // Can't validate candle size in this case
            return atrViolation;
        }

        Number prevAbsDif = (prevBar.getOpen().subtract(prevBar.getClose())).abs();

        Number relativeThreshold = prevAbsDif.multiply(new Number(relativeSize).getValue());

        boolean isEngulfing = absDif.compareTo(relativeThreshold) > 0;

        return atrViolation && isEngulfing;
    }

    @Override
    public double getValue() {
        return values.isEmpty() ? 0 : values.getLast().getValue();
    }

    @Override
    public double getValue(int index) {
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
        return atr.isReady();
    }

    @Override
    public int getRequiredPeriods() {
        return atrPeriod;
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        // We don't need to have an event publisher for indicators that an indicator may use internally.
        // This will emit events that may not actually be relevant for the strategy
        // atr.setEventPublisher(eventPublisher);
    }

    @Override
    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
        atr.setStrategyId(strategyId);
    }
}