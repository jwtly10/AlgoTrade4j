package dev.jwtly10.liveapi.executor;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.AccountEvent;
import dev.jwtly10.core.event.types.LiveAnalysisEvent;
import dev.jwtly10.core.event.types.async.AsyncTradesEvent;
import dev.jwtly10.core.exception.RiskException;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.external.notifications.Notifier;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.liveapi.model.Stats;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyService;
import dev.jwtly10.marketdata.common.BrokerClient;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class LiveStateManager {
    private final BrokerClient brokerClient;
    private final AccountManager accountManager;
    private final TradeManager tradeManager;
    private final EventPublisher eventPublisher;
    private final Strategy strategy;
    private final Instrument instrument;
    private final LiveStrategyService liveStrategyService;
    private final PerformanceAnalyser performanceAnalyser;
    private final Notifier notifier;

    // Some error handling for state updates
    // To prevent spamming of notifications
    private final static int MAX_ERROR_NOTIFICATIONS = 5;
    private int errorNotificationCount = 0;

    public LiveStateManager(BrokerClient brokerClient, AccountManager accountManager, TradeManager tradeManager,
                            EventPublisher eventPublisher, Strategy strategy, Instrument instrument, LiveStrategyService liveStrategyService, Notifier notifier) {
        this.brokerClient = brokerClient;
        this.accountManager = accountManager;
        this.tradeManager = tradeManager;
        this.instrument = instrument;
        this.eventPublisher = eventPublisher;
        this.strategy = strategy;
        this.liveStrategyService = liveStrategyService;
        this.performanceAnalyser = new PerformanceAnalyser();
        this.notifier = notifier;
    }

    /**
     * Update the trade and account states of the strategy
     * TODO, refactor this to use the same retryable stream logic we have implemented for streaming prices and transactions
     * // TODO: Note. Risk management logic depends on this data being up to date. so we need to have a way to shutdown if we can't get this data
     * Currently if this fails, there is no retry
     */
    public void updateState() {
        try {
            // Update account information
            Account accountInfo = brokerClient.getAccountInfo();
            accountManager.updateAccountInfo(accountInfo);

            // Update trade information
            List<Trade> trades = brokerClient.getAllTrades();
            List<Trade> openTrades = trades.stream().filter(trade -> trade.getClosePrice() == Number.ZERO || trade.getClosePrice() == null).collect(Collectors.toList());
            tradeManager.updateAllTrades(trades);
            tradeManager.updateOpenTrades(openTrades);

            // Update any WS clients with latest trade results
            eventPublisher.publishEvent(new AsyncTradesEvent(strategy.getStrategyId(), instrument, tradeManager.getAllTrades()));

            // TODO: Need to refactor some account logic
            eventPublisher.publishEvent(new AccountEvent(strategy.getStrategyId(), accountManager.getAccount()));

            // Check for risk management
            if (accountManager.getEquity() < (accountManager.getInitialBalance() * 0.1)) {
                throw new RiskException("Equity below 10%. Stopping strategy.");
            }

            // Do stats calculations for the strategy, after everything has been updated and events fired
            var startTime = System.currentTimeMillis();
            runPerformanceAnalysis();
            var finishTime = System.currentTimeMillis();

            // If it took more than 2 seconds to calculate stats, log it
            if (finishTime - startTime > 2000) {
                log.warn("Performance analysis took longer than 2 seconds: {}ms", finishTime - startTime);
            }

            eventPublisher.publishEvent(new LiveAnalysisEvent(strategy.getStrategyId(), instrument, performanceAnalyser, accountManager.getAccount()));

            // Reset error notification count, alert if it was previously blocked
            if (errorNotificationCount >= MAX_ERROR_NOTIFICATIONS) {
                notifier.sendSysNotification("Update trade state Error notifications re-enabled for strategy: " + strategy.getStrategyId(), true);
            }
            errorNotificationCount = 0;
        } catch (Exception e) {
            log.error("Error updating state for strategy ID: {}", strategy.getStrategyId(), e);

            if (errorNotificationCount < MAX_ERROR_NOTIFICATIONS) {
                notifier.sendSysErrorNotification("Error updating state for strategy (#" + errorNotificationCount + "): " + strategy.getStrategyId(), e, true);
                errorNotificationCount++;
            }
        }
    }

    private void runPerformanceAnalysis() {
        // Calculate stats
        Stats stats = new Stats();
        performanceAnalyser.calculateStatistics(tradeManager.getAllTrades(), accountManager.getAccount().getInitialBalance());

        DecimalFormat df = new DecimalFormat("#.##");
        stats.setAccountBalance(Double.parseDouble(df.format(accountManager.getBalance())));
        stats.setOpenTradeProfit(Double.parseDouble(df.format(performanceAnalyser.getOpenTradeProfit())));
        stats.setProfit(Double.parseDouble(df.format(performanceAnalyser.getTotalNetProfit())));
        stats.setTotalTrades(performanceAnalyser.getTotalTradeInclOpen());
        stats.setOpenTrades(performanceAnalyser.getOpenTrades());
        stats.setWinRate(Double.parseDouble(df.format((performanceAnalyser.getLongWinPercentage() + performanceAnalyser.getShortWinPercentage()) / 2)));
        stats.setProfitFactor(Double.parseDouble(df.format(performanceAnalyser.getProfitFactor())));
        stats.setSharpeRatio(Double.parseDouble(df.format(performanceAnalyser.getSharpeRatio())));

        liveStrategyService.updateStrategyStats(strategy.getStrategyId(), stats);
    }
}