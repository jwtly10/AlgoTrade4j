package dev.jwtly10.core.strategy;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.external.news.StrategyNewsUtil;
import dev.jwtly10.core.external.notifications.Notifier;
import dev.jwtly10.core.indicators.Indicator;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.core.risk.RiskManagementService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a trading strategy in the AlgoTrade4j framework.
 */
public interface Strategy {

    /**
     * Called once before the strategy processing starts.
     * Abstracts away the initialisation of the strategy, and required dependencies.
     *
     * @param series              The initial BarSeries available at strategy start.
     * @param dataManager         The DataManager instance for accessing market data.
     * @param accountManager      The AccountManager instance for managing account balances.
     * @param tradeManager        The TradeManager instance for executing trades.
     * @param eventPublisher      The EventPublisher instance for publishing events.
     * @param riskManager         The RiskManagementService instance for managing risk.
     * @param performanceAnalyser The PerformanceAnalyser instance for analysing strategy performance.
     * @param notifier            The Notifier instance for sending notifications. Can be null for strategies that do not require notifications (backtesting)
     * @param strategyNewsUtil    The strategy news util instance for accessing news data from the strategy
     */
    void onInit(BarSeries series, DataManager dataManager, AccountManager accountManager, TradeManager tradeManager, EventPublisher eventPublisher, RiskManagementService riskManager, PerformanceAnalyser performanceAnalyser, Notifier notifier, StrategyNewsUtil strategyNewsUtil);

    /**
     * Called once after the strategy processing ends.
     * This method is used by the system to do strategy clean up and analysis.
     */
    void onDeInit();

    /**
     * Called once after the strategy processing starts.
     * Use this method to perform any custom initialisation logic.
     */
    void onStart();

    /**
     * Returns a unique identifier for the strategy. TODO: Should this be unique? I guess callers can handle that - adding some unique key in the case of optimisation etc where multiple runs are happening TBC.
     *
     * @return The unique identifier for the strategy.
     */
    String getStrategyId();

    /**
     * Called on each bar close (the bar is completed)
     * This method can be used to perform strategy logic based on the most recent bar of completed market data.
     * This method should ideally NOT throw.
     * Any exceptions should be caught and logged within the method.
     *
     * @param bar The most recent bar of market data.
     */
    void onBarClose(Bar bar);

    /**
     * Called on each tick of market data.
     * This method can be used to perform additional processing on each tick.
     * This method should ideally NOT throw.
     * Any exceptions should be caught and logged within the method.
     *
     * @param tick       The most recent tick of market data.
     * @param currentBar The current bar of market data (may be incomplete)
     */
    void onTick(Tick tick, Bar currentBar);

    /**
     * Called when a trade is closed.
     * This method can be used to perform additional processing when a trade is closed.
     * This method should ideally NOT throw.
     * Any exceptions should be caught and logged within the method.
     *
     * @param trade The trade that was closed
     */
    void onTradeClose(Trade trade);

    /**
     * Called on each new day of market data.
     * This method can be used to perform additional processing on each day or trigger some logic.
     * This method should ideally NOT throw.
     * Any exceptions should be caught and logged within the method.
     *
     * @param newDay The datetime of the first tick data that triggered a new day
     */
    void onNewDay(ZonedDateTime newDay);

    /**
     * Returns the list of indicators set in the strategy
     *
     * @return list of indicators set in the strategy
     */
    List<Indicator> getIndicators();

    /**
     * Called once after the strategy processing ends.
     * Use this method to perform any cleanup tasks or final calculations.
     */
    void onEnd();

    /**
     * Set the parameters for the strategy
     *
     * @param parameters The parameters to set
     * @throws IllegalAccessException If the parameters are not valid
     */
    void setParameters(Map<String, String> parameters) throws IllegalAccessException;

    /**
     * Set the notification chat id for a specific strategy
     *
     * @param chatId The chat id to send notifications to
     */
    void setNotificationChatId(String chatId);

    /**
     * Get flag to enable system notifications
     *
     * @return if system notifications are enabled
     */
    boolean canUseSystemNotifications();
}