package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.model.Tick;

/**
 * Interface for managing the state of trades.
 */
public interface TradeStateManager {
    /**
     * Updates the states of trades based on the provided account manager, trade manager, and tick data.
     *
     * @param tradeManager   the trade manager handling trade-related operations
     * @param tick           the tick data used to update trade states
     */
    void updateTradeStates(TradeManager tradeManager, Tick tick);

    /**
     * Updates the states of account based on the provided account manager, trade manager
     *
     * @param accountManager the account manager handling account-related operations
     * @param tradeManager   the trade manager handling trade-related operations
     */
    void updateAccountState(AccountManager accountManager, TradeManager tradeManager);
}