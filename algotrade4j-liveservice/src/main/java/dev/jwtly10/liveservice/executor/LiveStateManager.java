package dev.jwtly10.liveservice.executor;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.event.AccountEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.exception.RiskException;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.marketdata.common.BrokerClient;

import java.util.List;

public class LiveStateManager {
    private final BrokerClient brokerClient;
    private final AccountManager accountManager;
    private final TradeManager tradeManager;
    private final EventPublisher eventPublisher;
    private final String strategyId;

    public LiveStateManager(BrokerClient brokerClient, AccountManager accountManager, TradeManager tradeManager,
                            EventPublisher eventPublisher, String strategyId) {
        this.brokerClient = brokerClient;
        this.accountManager = accountManager;
        this.tradeManager = tradeManager;
        this.eventPublisher = eventPublisher;
        this.strategyId = strategyId;
    }

    /**
     * Update the trade and account states of the strategy
     */
    public void updateState() {
        try {
            // Update account information
            Account accountInfo = brokerClient.getAccountInfo();
            accountManager.updateAccountInfo(accountInfo);

            // Update trade information
            List<Trade> trades = brokerClient.getOpenTrades();
            tradeManager.updateOpenTrades(trades);

            // Publish events
            eventPublisher.publishEvent(new AccountEvent(strategyId, accountManager.getAccount()));
            tradeManager.getOpenTrades().values().forEach(trade -> {
            });

            // Check for risk management
            if (accountManager.getEquity() < (accountManager.getInitialBalance() * 0.1)) {
                throw new RiskException("Equity below 10%. Stopping strategy.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating state for strategy: " + strategyId, e);
        }
    }
}