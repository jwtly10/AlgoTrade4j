package dev.jwtly10.core.account;

import lombok.Data;

@Data
public class DefaultAccountManager implements AccountManager {
    private final Account account;

    public DefaultAccountManager(double initialBalance, double balance, double equity) {
        this.account = new Account(initialBalance, balance, equity);
    }

    // In cases of a new, empty account
    public DefaultAccountManager(double initialBalance) {
        this.account = new Account(initialBalance);
    }

    @Override
    public double getBalance() {
        return account.getBalance();
    }

    @Override
    public void setBalance(double balance) {
        account.setBalance(balance);
    }

    @Override
    public double getEquity() {
        return account.getEquity();
    }

    @Override
    public void setEquity(double equity) {
        account.setEquity(equity);
    }

    @Override
    public double getInitialBalance() {
        return account.getInitialBalance();
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public void updateAccountInfo(Account account) {
        // We ignore the initial balance here, as its set at the time of account creation, so is irrelevant in future updates
        this.account.setBalance(account.getBalance());
        this.account.setEquity(account.getEquity());
    }

    @Override
    public double getOpenPositionValue() {
        return getEquity() - getBalance();
    }
}