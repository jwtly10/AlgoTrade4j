package dev.jwtly10.core.strategy;

import dev.jwtly10.core.*;
import dev.jwtly10.core.event.EventPublisher;

import java.lang.reflect.Constructor;

public abstract class BaseStrategy implements Strategy {
    protected final String strategyId;
    protected PriceFeed priceFeed;
    protected TradeManager tradeManager;
    protected BarSeries barSeries;
    protected EventPublisher eventPublisher;

    public BaseStrategy(String strategyId) {
        this.strategyId = strategyId;
    }

    @Override
    public void onInit(BarSeries series, PriceFeed priceFeed, TradeManager tradeManager, EventPublisher eventPublisher) {
        this.barSeries = series;
        this.priceFeed = priceFeed;
        this.tradeManager = tradeManager;
        this.eventPublisher = eventPublisher;
        initIndicators();
    }

    /**
     * Custom initialization method that can be overridden by strategy implementations.
     * This method is called after indicators are initialized but before the strategy starts processing bars.
     */
    public void onStart() {
        // Default implementation is empty
        // Strategy developers can override this method to add custom initialization logic
    }

    /**
     * Custom de-initialization method that can be overridden by strategy implementations.
     * This method is called after the strategy has finished processing bars.
     */
    protected void initIndicators() {
        // Default implementation is empty
        // Strategy developers can use this to instantiate and configure indicators, using the createIndicator method
    }

    @Override
    public String getStrategyId() {
        return strategyId;
    }

    // Factory method for creating indicators
    protected <T extends Indicator> T createIndicator(Class<T> indicatorClass, Object... params) {
        try {
            Constructor<T> constructor = indicatorClass.getConstructor(params.getClass());
            T indicator = constructor.newInstance(params);
            indicator.setEventPublisher(eventPublisher);
            indicator.setStrategyId(strategyId);
            return indicator;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create indicator: " + indicatorClass.getSimpleName(), e);
        }
    }
}