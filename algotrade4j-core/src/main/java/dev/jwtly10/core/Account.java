package dev.jwtly10.core;

import lombok.Getter;

@Getter
public class Account {
    private final Number initialBalance;
    private Number balance;
    private Number equity;
    private Number openPositionValue;

    public Account(Number initialBalance) {
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.equity = initialBalance;
        this.openPositionValue = Number.ZERO;
    }

    void updateBalance(Number newBalance) {
        this.balance = newBalance;
    }

    void updateEquity(Number newEquity) {
        this.equity = newEquity;
    }

    void updateOpenPositionValue(Number newOpenPositionValue) {
        this.openPositionValue = newOpenPositionValue;
    }
}