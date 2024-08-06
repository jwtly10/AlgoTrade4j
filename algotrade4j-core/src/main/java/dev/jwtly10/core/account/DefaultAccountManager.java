package dev.jwtly10.core.account;

import dev.jwtly10.core.model.Number;
import lombok.Data;

@Data
public class DefaultAccountManager implements AccountManager {
    private Number balance;
    private Number equity;
    private Number initialBalance;

    public DefaultAccountManager(Number balance, Number equity, Number initialBalance) {
        this.balance = balance;
        this.equity = equity;
        this.initialBalance = initialBalance;
    }
}