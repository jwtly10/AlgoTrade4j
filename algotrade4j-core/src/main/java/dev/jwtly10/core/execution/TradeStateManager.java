package dev.jwtly10.core.execution;

import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.account.AccountManager;

public interface TradeStateManager {
    void updateTradeStates(AccountManager accountManager, TradeManager tradeManager, Tick tick);
}