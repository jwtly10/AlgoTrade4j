package dev.jwtly10.core.account;

import dev.jwtly10.core.model.Number;

public interface AccountManager {
    Number getBalance();

    void setBalance(Number balance);

    Number getEquity();

    void setEquity(Number equity);

    Number getInitialBalance();
}