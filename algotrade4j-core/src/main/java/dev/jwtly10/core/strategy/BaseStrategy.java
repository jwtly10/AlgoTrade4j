package dev.jwtly10.core.strategy;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.LogEvent;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.external.news.StrategyNewsUtil;
import dev.jwtly10.core.external.notifications.Notifier;
import dev.jwtly10.core.indicators.Indicator;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.risk.RiskManagementService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.*;

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
    @Getter
    private final List<Indicator> indicators = new ArrayList<>();
    /**
     * The instrument associated with the strategy.
     */
    public Instrument SYMBOL;
    /**
     * The series of bars used by the strategy.
     */
    protected BarSeries barSeries;
    /**
     * The event publisher used by the strategy.
     */
    protected EventPublisher eventPublisher;
    /**
     * The risk manager used by the strategy. Has access to the broker information of the running strategy
     */
    protected RiskManagementService riskManager;
    /**
     * The news util used by the strategy.
     */
    private StrategyNewsUtil strategyNewsUtil;
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
     * The external notifier used by the strategy.
     */
    private Notifier notifier;
    /**
     * Flag to determine if the strategy should allow system notifications
     * Used to ensure the strategy runs can be monitored via an external system
     */
    private boolean useSystemNotifications = false;

    /**
     * Flag to determine if the strategy should use custom notifications
     * These are private notifications that can be sent by the strategy developer
     */
    private boolean useCustomNotifications = false;

    /**
     * The chat ID of the notifier.
     */
    private String notifierChatId;

    /**
     * Constructs a BaseStrategy with the specified strategy ID.
     *
     * @param strategyId the unique identifier of the strategy
     */
    public BaseStrategy(String strategyId) {
        this.strategyId = strategyId;
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
    public void onInit(BarSeries series, DataManager dataManager, AccountManager accountManager, TradeManager tradeManager, EventPublisher eventPublisher, RiskManagementService riskManager, PerformanceAnalyser performanceAnalyser, Notifier notifier, StrategyNewsUtil strategyNewsUtil) {
        this.barSeries = series;
        this.dataManager = dataManager;
        this.accountManager = accountManager;
        this.tradeManager = tradeManager;
        this.eventPublisher = eventPublisher;
        this.SYMBOL = dataManager.getInstrument();
        this.performanceAnalyser = performanceAnalyser;
        this.strategyNewsUtil = strategyNewsUtil;
        this.riskManager = riskManager;
        if (notifier == null) {
            log.warn("Notifier is null. Notifications will not be sent for strategy '{}'", strategyId);
        }
        this.notifier = notifier;
        try {
            ParameterHandler.initialize(this);
        } catch (IllegalAccessException e) {
            // TODO: Make this better - we should stop this and send event
            log.error("Error initializing strategy parameters: {}", e.getMessage(), e);
            throw new RuntimeException("Error initializing strategy parameters", e);
        }
        initIndicators();
        eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Strategy '%s' initialized", strategyId));
    }

    /**
     * Called to de initialize the strategy. Used by the system
     */
    @Override
    public void onDeInit() {
        log.info("Strategy run {} completed", strategyId);
        eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Strategy '%s' de-initialized", strategyId));
    }

    /**
     * Create trade parameters for a trade
     * This is the recommend way of creating a trade, all trades in a strategy (to comply with good risk management)
     * Should open trades based on a given risk ratio, given a defined tick size, this way trades are consistent
     *
     * @param instrument     The instrument to trade
     * @param stopLossTicks  The number of ticks for the stop loss
     * @param isLong         Whether the trade is long or short
     * @param riskRatio      The risk ratio of SL to TP
     * @param riskPercentage The percentage of balance to risk
     * @param balanceToRisk  The balance to risk
     * @return The trade parameters, ready to be used
     */
    protected TradeParameters createTradeParameters(Instrument instrument, int stopLossTicks, boolean isLong, double riskRatio, double riskPercentage, double balanceToRisk) {
        TradeParameters params = new TradeParameters();
        params.setInstrument(instrument);
        params.setEntryPrice(isLong ? Ask() : Bid());
        Number stopLossPrice = getStopLossGivenInstrumentPriceDir(instrument, params.getEntryPrice(), stopLossTicks, isLong);
        params.setStopLoss(stopLossPrice);
        params.setRiskRatio(riskRatio);
        params.setRiskPercentage(riskPercentage);
        params.setBalanceToRisk(balanceToRisk);
        return params;
    }

    /**
     * Opens a long position with the specified trade parameters.
     * This method will throw an exception if the risk manager does not allow the trade.
     * The risk manager checks the risk profile of the strategy and the account balance before allowing the trade.
     *
     * @param params the trade parameters
     * @return Optional of the trade
     */
    public Optional<Trade> openLong(TradeParameters params) {
        try {
            Trade openedTrade = tradeManager.openLong(params);
            sysOpenTradeNotif(openedTrade);
            eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Trade [%s] opened long for strategy '%s' at price", openedTrade.getId(), strategyId, openedTrade.getEntryPrice()));
            return Optional.of(openedTrade);
        } catch (Exception e) {
            sysErrorNotif("Error opening long trade for strategy '" + strategyId + "'", e);
            eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.ERROR, "Error opening short trade: %s ", e.getMessage()));
            return Optional.empty();
        }
    }

    /**
     * Opens a short position with the specified trade parameters.
     * This method will throw an exception if the risk manager does not allow the trade.
     * The risk manager checks the risk profile of the strategy and the account balance before allowing the trade.
     *
     * @param params the trade parameters
     * @return Optional of the trade
     */
    public Optional<Trade> openShort(TradeParameters params) {
        try {
            Trade openedTrade = tradeManager.openShort(params);
            sysOpenTradeNotif(openedTrade);
            eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Trade [%s] opened short for strategy '%s' at price", openedTrade.getId(), strategyId, openedTrade.getEntryPrice()));
            return Optional.of(openedTrade);
        } catch (Exception e) {
            sysErrorNotif("Error opening short trade for strategy '" + strategyId + "'", e);
            eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.ERROR, "Error opening short trade: %s ", e.getMessage()));
            return Optional.empty();
        }
    }

    /**
     * Closes all open trades for the strategy.
     *
     * @param reason the reason for closing the trades
     */
    public void manuallyCloseAllOpenPositions(String reason) {
        var trades = tradeManager.getOpenTrades();
        // If no trades are open we can return, don't bother logging
        if (trades.isEmpty()) {
            return;
        }
        log.info("Strategy requested to manually close all trades for reason: {}", reason);
        eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Manually closing all trades for strategy '%s' for reason: %s", strategyId, reason));
        for (Trade trade : trades.values()) {
            try {
                tradeManager.closePosition(trade.getId(), true);
            } catch (Exception e) {
                sysErrorNotif("Error closing trade for strategy '" + strategyId + "'", e);
                eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.ERROR, "Error closing trade: %s ", e.getMessage()));
            }
        }
        sysAllPositionsClosedNotif(reason);
    }

    /**
     * Turns on the use of system notifications for a strategy
     * System notifications include automated trade open/close notifications
     */
    public void useSystemNotifications() {
        this.useSystemNotifications = true;
    }

    /**
     * Turns on the use of custom notifications for a strategy
     * Custom notifications are private notifications that can be sent by the strategy developer
     */
    public void useCustomNotifications() {
        this.useCustomNotifications = true;
    }


    @Override
    public boolean canUseSystemNotifications() {
        return this.useSystemNotifications;
    }

    /**
     * Returns the initial balance of the account.
     *
     * @return the initial balance
     */
    public double getInitialBalance() {
        return accountManager.getInitialBalance();
    }

    /**
     * Returns the current balance of the account.
     *
     * @return the current balance
     */
    public double getBalance() {
        return accountManager.getBalance();
    }

    /**
     * Returns the current equity of the account.
     *
     * @return the current equity
     */
    public double getEquity() {
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
     * Utility method to get the stop loss price given the instrument, price, pips and direction.
     *
     * @param instrument the instrument
     * @param price      the price
     * @param ticks      the ticks
     * @param isLong     the direction
     * @return the stop loss price
     */
    public Number getStopLossGivenInstrumentPriceDir(Instrument instrument, Number price, int ticks, boolean isLong) {
        if (ticks < 100) {
            log.warn("Stop loss ticks is less than 100. This may be a mistake, ticks should be x10 pips. Ticks: {}", ticks);
        }
        Number pipValue = new Number(ticks * instrument.getBrokerConfig(tradeManager.getBroker()).getPipValue());
        return isLong ? price.subtract(pipValue) : price.add(pipValue);
    }

    /**
     * Sends a notification message to external notifier implementation
     * By default all notifications will use HTML mode
     * Uses the chat id set by the strategy
     *
     * @param message the message
     */
    public void sendCustomNotification(String message) {
        if (notifier != null && !notifierChatId.isEmpty() && useCustomNotifications) {
            notifier.sendNotification(notifierChatId, message, true);
        }
    }

    /**
     * Sets the parameters of the strategy.
     *
     * @param parameters the parameters
     * @throws IllegalAccessException if the parameters cannot be set
     */
    public void setParameters(Map<String, String> parameters) throws IllegalAccessException {
        ParameterHandler.setParameters(this, parameters);
    }

    @Override
    public void setNotificationChatId(String chatId) {
        this.notifierChatId = chatId;
    }

    /**
     * Returns the current parameters of the strategy.
     *
     * @return the current parameters
     * @throws IllegalAccessException if the parameters cannot be accessed
     */
    public Map<String, String> getCurrentParameters() throws IllegalAccessException {
        Map<String, String> currentParams = new HashMap<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter param = field.getAnnotation(Parameter.class);
                field.setAccessible(true);
                currentParams.put(param.name(), field.get(this).toString());
            }
        }
        return currentParams;
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

    @Override
    public void onNewDay(ZonedDateTime newDay) {
        // Default implementation is empty
        // Strategy developers can override this method to add custom logic on new day event
    }

    @Override
    public void onTradeClose(Trade trade) {
        systemCloseTradeNotif(trade);
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
        log.trace("Creating indicator: '{}' with params: '{}'", indicatorClass.getSimpleName(), params);

        // TODO: We should do something better than this.
        if ((indicatorClass.getSimpleName().equals("iSMA") || indicatorClass.getSimpleName().equals("iEMA")) & params[0].equals(0)) {
            // This means the user needs to be aware that indicators are null if they are set to the nullable value
            // So access them will fail
            log.trace("An iSMA has value 0. Will not create indicator");
            return null;
        }

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

            indicators.add(indicator);

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
            log.trace("Constructor found with types: {}", constructor.getParameterTypes());
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

    /**
     * Wrapper for trade open notifications
     *
     * @param trade The trade that was opened
     */
    private void sysOpenTradeNotif(Trade trade) {
        if (useSystemNotifications && notifier != null) {
            String tradeType = trade.isLong() ? "Long" : "Short";
            String message = String.format(
                    "🚀 <b>New %s trade opened for '%s' !</b>\n" +
                            "<b>Trade ID:</b> #%d\n" +
                            "<b>Instrument:</b> %s\n" +
                            "<b>Entry Price:</b> %.2f\n",
                    tradeType,
                    strategyId,
                    trade.getId(),
                    trade.getInstrument(),
                    trade.getEntryPrice().doubleValue()
            );

            notifier.sendSysNotification(message, true);
        }
    }

    private void sysAllPositionsClosedNotif(String reason) {
        if (useSystemNotifications && notifier != null) {
            String message = String.format(
                    "🔒 <b>All positions closed for '%s' !</b>\n" +
                            "<b>Reason:</b> '%s'\n",
                    strategyId,
                    reason
            );

            notifier.sendSysNotification(message, true);
        }
    }

    /**
     * Wrapper for trade close notifications
     *
     * @param trade The trade that was closed
     */
    private void systemCloseTradeNotif(Trade trade) {
        if (useSystemNotifications && notifier != null) {
            String tradeType = trade.isLong() ? "Long" : "Short";
            String profitStatus = trade.getProfit() >= 0 ? "Profit" : "Loss";
            String message = String.format(
                    "🔔 <b>%s Trade Closed for '%s' !</b>\n" +
                            "<b>Trade ID:</b> #%d\n" +
                            "<b>Instrument:</b> %s\n" +
                            "<b>Entry Price:</b> %.2f\n" +
                            "<b>Close Price:</b> %.2f\n" +
                            "<b>Profit/Loss:</b> $%.2f %s\n",
                    tradeType,
                    strategyId,
                    trade.getId(),
                    trade.getInstrument(),
                    trade.getEntryPrice().doubleValue(),
                    trade.getClosePrice().doubleValue(),
                    Math.abs(trade.getProfit()),
                    profitStatus
            );

            notifier.sendSysNotification(message, true);
        }
    }

    /**
     * Wrapper for trade error notifications
     *
     * @param message The message to send
     * @param e       The exception that caused the error
     */
    private void sysErrorNotif(String message, Exception e) {
        if (useSystemNotifications && notifier != null) {
            notifier.sendSysErrorNotification(message, e, true);
        }
    }
}