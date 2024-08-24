package dev.jwtly10.core.account;

import dev.jwtly10.core.model.Number;
import lombok.Data;

@Data
public class DefaultAccountManager implements AccountManager {
    private final Account account;

    public DefaultAccountManager(Number initialBalance, Number balance, Number equity) {
        this.account = new Account(initialBalance, balance, equity);
    }

    // In cases of a new, empty account
    public DefaultAccountManager(Number initialBalance) {
        this.account = new Account(initialBalance);
    }

    @Override
    public Number getBalance() {
        return account.getBalance().roundMoneyDown();
    }

    @Override
    public void setBalance(Number balance) {
        account.setBalance(balance.roundMoneyDown());
    }

    @Override
    public Number getEquity() {
        return account.getEquity().roundMoneyDown();
    }

    @Override
    public void setEquity(Number equity) {
        account.setEquity(equity.roundMoneyDown());
    }

    @Override
    public Number getInitialBalance() {
        return account.getInitialBalance().roundMoneyDown();
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public Number getOpenPositionValue() {
        return getEquity().subtract(getBalance()).roundMoneyDown();
    }
}