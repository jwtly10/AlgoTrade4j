package dev.jwtly10.liveservice.executor;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.event.AccountEvent;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.async.AsyncTradesEvent;
import dev.jwtly10.core.exception.RiskException;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.marketdata.common.BrokerClient;

import java.util.List;
import java.util.stream.Collectors;

public class LiveStateManager {
    private final BrokerClient brokerClient;
    private final AccountManager accountManager;
    private final TradeManager tradeManager;
    private final EventPublisher eventPublisher;
    private final String strategyId;
    private final Instrument instrument;

    public LiveStateManager(BrokerClient brokerClient, AccountManager accountManager, TradeManager tradeManager,
                            EventPublisher eventPublisher, String strategyId, Instrument instrument) {
        this.brokerClient = brokerClient;
        this.accountManager = accountManager;
        this.tradeManager = tradeManager;
        this.instrument = instrument;
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
            List<Trade> trades = brokerClient.getAllTrades();
            List<Trade> openTrades = trades.stream().filter(trade -> trade.getClosePrice() == Number.ZERO || trade.getClosePrice() == null).collect(Collectors.toList());
            tradeManager.updateAllTrades(trades);
            tradeManager.updateOpenTrades(openTrades);

            // Update any WS clients with latest trade results
            eventPublisher.publishEvent(new AsyncTradesEvent(strategyId, instrument, tradeManager.getAllTrades()));

            // TODO: Need to refactor some account logic
            eventPublisher.publishEvent(new AccountEvent(strategyId, accountManager.getAccount()));

            // Check for risk management
            if (accountManager.getEquity() < (accountManager.getInitialBalance() * 0.1)) {
                throw new RiskException("Equity below 10%. Stopping strategy.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating state for strategy: " + strategyId, e);
        }
    }
}