package dev.jwtly10.core.strategy;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.*;
import dev.jwtly10.core.event.EventPublisher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;

@Slf4j
@Getter
public abstract class BaseStrategy implements Strategy {
    protected final String strategyId;
    protected PriceFeed priceFeed;
    protected BarSeries barSeries;
    protected EventPublisher eventPublisher;
    private TradeManager tradeManager;

    public BaseStrategy(String strategyId) {
        this.strategyId = strategyId;
    }

    public String openLong(TradeParameters params) {
        return tradeManager.openLong(params);
    }

    public String openShort(TradeParameters params) {
        return tradeManager.openShort(params);
    }

//    public Number getBalance() {
//        return tradeManager.getBalance();
//    }
//
//    public Number getEquity() {
//        return tradeManager.getEquity();
//    }

    public Bar getLastBar() {
        return barSeries.getLastBar();
    }

    public Bar getBar(int index) {
        return barSeries.getBar(index);
    }


    public Number Ask(String symbol) {
        return priceFeed.getAsk(symbol);
    }

    public Number Bid(String symbol) {
        return priceFeed.getBid(symbol);
    }


    @Override
    public void onInit(BarSeries series, PriceFeed priceFeed, TradeManager tradeManager, EventPublisher eventPublisher) {
        log.debug("Initializing strategy from BaseStrategy: {}", strategyId);
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
        log.info("Creating indicator: '{}' with params: '{}'", indicatorClass.getSimpleName(), params);
        try {
            Class<?>[] paramTypes = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                if (params[i] == null) {
                    paramTypes[i] = Object.class;
                } else {
                    Class<?> paramClass = params[i].getClass();
                    paramTypes[i] = getPrimitiveType(paramClass);
                }
            }

            // Try to find a matching constructor
            Constructor<T> constructor = findMatchingConstructor(indicatorClass, paramTypes);
            if (constructor == null) {
                throw new NoSuchMethodException("No matching constructor found");
            }

            T indicator = constructor.newInstance(params);
            // Ensure deps are set
            indicator.setEventPublisher(eventPublisher);
            indicator.setStrategyId(strategyId);
            return indicator;
        } catch (Exception e) {
            log.error("Failed to create indicator '{}' for params '{}'", indicatorClass.getSimpleName(), params, e);
            throw new RuntimeException("Failed to create indicator: " + indicatorClass.getSimpleName(), e);
        }
    }

    // These methods are used to handle primitive types, in order to find the correct constructor
    private Class<?> getPrimitiveType(Class<?> cls) {
        if (cls == Integer.class) return int.class;
        if (cls == Long.class) return long.class;
        if (cls == Float.class) return float.class;
        if (cls == Double.class) return double.class;
        if (cls == Boolean.class) return boolean.class;
        if (cls == Byte.class) return byte.class;
        if (cls == Character.class) return char.class;
        if (cls == Short.class) return short.class;
        return cls;
    }

    private <T> Constructor<T> findMatchingConstructor(Class<T> cls, Class<?>[] paramTypes) {
        Constructor<?>[] constructors = cls.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] ctorParamTypes = constructor.getParameterTypes();
            if (isAssignable(ctorParamTypes, paramTypes)) {
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

    private boolean isAssignable(Class<?>[] ctorParamTypes, Class<?>[] paramTypes) {
        if (ctorParamTypes.length != paramTypes.length) {
            return false;
        }
        for (int i = 0; i < ctorParamTypes.length; i++) {
            if (!ctorParamTypes[i].isAssignableFrom(paramTypes[i]) &&
                    !isPrimitiveAssignable(ctorParamTypes[i], paramTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isPrimitiveAssignable(Class<?> ctorParamType, Class<?> paramType) {
        return (ctorParamType.isPrimitive() && getPrimitiveType(paramType) == ctorParamType) ||
                (paramType.isPrimitive() && getPrimitiveType(ctorParamType) == paramType);
    }
}