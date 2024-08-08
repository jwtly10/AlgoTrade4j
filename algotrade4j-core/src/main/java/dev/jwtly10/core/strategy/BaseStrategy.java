package dev.jwtly10.core.strategy;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.indicators.Indicator;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.TradeParameters;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;

/**
 * Abstract base class for trading strategies.
 * All Strategies should extend this class and implement the onStart method if needed
 * This base strategy exposes utility methods for strategies such as opening trades, getting account information, and accessing bar data.
 */
@Slf4j
@Getter
public abstract class BaseStrategy implements Strategy {
    /**
     * The unique identifier of the strategy.
     */
    protected final String strategyId;

    /**
     * The symbol associated with the strategy.
     */
    public String SYMBOL;

    /**
     * The series of bars used by the strategy.
     */
    protected BarSeries barSeries;

    /**
     * The event publisher used by the strategy.
     */
    protected EventPublisher eventPublisher;

    /**
     * The trade manager used by the strategy.
     */
    private TradeManager tradeManager;

    /**
     * The data manager used by the strategy.
     */
    private DataManager dataManager;

    /**
     * The account manager used by the strategy.
     */
    private AccountManager accountManager;

    /**
     * The performance analyser used by the strategy.
     */
    private PerformanceAnalyser performanceAnalyser;

    /**
     * Constructs a BaseStrategy with the specified strategy ID.
     *
     * @param strategyId the unique identifier of the strategy
     */
    public BaseStrategy(String strategyId) {
        this.strategyId = strategyId;
    }

    /**
     * Opens a long position with the specified trade parameters.
     *
     * @param params the trade parameters
     * @return the trade ID
     */
    public Integer openLong(TradeParameters params) {
        return tradeManager.openLong(params);
    }

    /**
     * Opens a short position with the specified trade parameters.
     *
     * @param params the trade parameters
     * @return the trade ID
     */
    public Integer openShort(TradeParameters params) {
        return tradeManager.openShort(params);
    }

    /**
     * Returns the initial balance of the account.
     *
     * @return the initial balance
     */
    public Number getInitialBalance() {
        return accountManager.getInitialBalance();
    }

    /**
     * Returns the current balance of the account.
     *
     * @return the current balance
     */
    public Number getBalance() {
        return accountManager.getBalance();
    }

    /**
     * Returns the current equity of the account.
     *
     * @return the current equity
     */
    public Number getEquity() {
        return accountManager.getEquity();
    }

    /**
     * Returns the last bar in the series.
     *
     * @return the last bar
     */
    public Bar getLastBar() {
        return barSeries.getLastBar();
    }

    /**
     * Returns the bar at the specified index.
     *
     * @param index the index of the bar
     * @return the bar at the specified index
     */
    public Bar getBar(int index) {
        return barSeries.getBar(index);
    }

    /**
     * Returns the current ask price.
     *
     * @return the current ask price
     */
    public Number Ask() {
        return dataManager.getCurrentAsk();
    }

    /**
     * Returns the current bid price.
     *
     * @return the current bid price
     */
    public Number Bid() {
        return dataManager.getCurrentBid();
    }

    /**
     * Initializes the strategy with the specified components.
     *
     * @param series         the bar series
     * @param dataManager    the data manager
     * @param accountManager the account manager
     * @param tradeManager   the trade manager
     * @param eventPublisher the event publisher
     */
    @Override
    public void onInit(BarSeries series, DataManager dataManager, AccountManager accountManager, TradeManager tradeManager, EventPublisher eventPublisher, PerformanceAnalyser performanceAnalyser) {
        log.debug("Initializing strategy from BaseStrategy: {}", strategyId);
        this.barSeries = series;
        this.dataManager = dataManager;
        this.accountManager = accountManager;
        this.tradeManager = tradeManager;
        this.eventPublisher = eventPublisher;
        this.SYMBOL = dataManager.getSymbol();
        this.performanceAnalyser = performanceAnalyser;
        initIndicators();
    }

    /**
     * Called to de initialize the strategy. Used by the system
     */
    @Override
    public void onDeInit() {

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
     * Called when the strategy processing ends.
     * This method is called after the strategy has finished running
     */
    @Override
    public void onEnd() {
        // Default implementation is empty
        // Strategy developers can override this method to add custom cleanup logic
    }

    /**
     * Strategy developers can override this method to initialize new indicators using the createIndicator method.
     */
    protected void initIndicators() {
        // Default implementation is empty
        // Strategy developers can use this to instantiate and configure indicators, using the createIndicator method
    }

    /**
     * Returns the unique identifier of the strategy.
     *
     * @return the strategy ID
     */
    @Override
    public String getStrategyId() {
        return strategyId;
    }

    /**
     * Factory method for creating indicators.
     *
     * @param indicatorClass the class of the indicator
     * @param params         the parameters for the indicator constructor
     * @param <T>            the type of the indicator
     * @return the created indicator
     */
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
            // Ensure dependencies are set
            indicator.setEventPublisher(eventPublisher);
            indicator.setStrategyId(strategyId);
            return indicator;
        } catch (Exception e) {
            log.error("Failed to create indicator '{}' for params '{}'", indicatorClass.getSimpleName(), params, e);
            throw new RuntimeException("Failed to create indicator: " + indicatorClass.getSimpleName(), e);
        }
    }

    /**
     * Returns the primitive type corresponding to the given class.
     *
     * @param cls the class
     * @return the primitive type, or the original class if it is not a wrapper type
     */
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

    /**
     * Finds a matching constructor for the given parameter types.
     *
     * @param cls        the class
     * @param paramTypes the parameter types
     * @param <T>        the type of the class
     * @return the matching constructor, or null if no matching constructor is found
     */
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

    /**
     * Checks if the given parameter types are assignable to the constructor parameter types.
     *
     * @param ctorParamTypes the constructor parameter types
     * @param paramTypes     the parameter types
     * @return true if the parameter types are assignable, false otherwise
     */
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

    /**
     * Checks if the given primitive types are assignable.
     *
     * @param ctorParamType the constructor parameter type
     * @param paramType     the parameter type
     * @return true if the primitive types are assignable, false otherwise
     */
    private boolean isPrimitiveAssignable(Class<?> ctorParamType, Class<?> paramType) {
        return (ctorParamType.isPrimitive() && getPrimitiveType(paramType) == ctorParamType) ||
                (paramType.isPrimitive() && getPrimitiveType(ctorParamType) == paramType);
    }
}