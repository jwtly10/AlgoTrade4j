package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;

/**
 * Interface for managing the state of trades.
 */
public interface TradeStateManager {
    /**
     * Updates the states of trades based on the provided account manager, trade manager, and tick data.
     *
     * @param tradeManager the trade manager handling trade-related operations
     * @param tick         the tick data used to update trade states
     */
    void updateTradeProfitStateOnTick(TradeManager tradeManager, Tick tick);

    /**
     * Updates the equity of an account on tick, based on the provided account manager and trade manager.
     *
     * @param accountManager the account manager handling account-related operations
     * @param tradeManager   the trade manager handling trade-related operations
     */
    void updateAccountEquityOnTick(AccountManager accountManager, TradeManager tradeManager);

    /**
     * Updates the balance of account on trade close, based on the provided trade and account manager.
     *
     * @param trade          the trade that is being closed
     * @param accountManager the account manager handling account-related operations
     */
    void updateBalanceOnTradeClose(Trade trade, AccountManager accountManager);
}