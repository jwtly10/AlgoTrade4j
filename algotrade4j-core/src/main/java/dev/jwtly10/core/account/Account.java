package dev.jwtly10.core.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    private final double initialBalance;
    private double balance;
    private double equity;

    public Account(double initialBalance) {
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.equity = initialBalance;
    }

    public Account(double initialBalance, double balance, double equity) {
        this.initialBalance = initialBalance;
        this.balance = balance;
        this.equity = equity;
    }
}