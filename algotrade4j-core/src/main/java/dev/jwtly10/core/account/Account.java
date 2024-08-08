package dev.jwtly10.core.account;

import dev.jwtly10.core.model.Number;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    private final Number initialBalance;
    private Number balance;
    private Number equity;

    public Account(Number initialBalance) {
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.equity = initialBalance;
    }

    public Account(Number initialBalance, Number balance, Number equity) {
        this.initialBalance = initialBalance;
        this.balance = balance;
        this.equity = equity;
    }
}